package com.osroyale;

import com.osroyale.content.WellOfGoodwill;
import com.osroyale.content.bloodmoney.BloodChestEvent;
import com.osroyale.content.clanchannel.ClanRepository;
import com.osroyale.content.itemaction.ItemActionRepository;
import com.osroyale.content.lms.LMSGameEvent;
import com.osroyale.content.lms.loadouts.LMSLoadoutManager;
import com.osroyale.content.lms.lobby.LMSLobbyEvent;
import com.osroyale.content.mysterybox.MysteryBox;
import com.osroyale.content.preloads.PreloadRepository;
import com.osroyale.content.shootingstar.ShootingStar;
import com.osroyale.content.skill.SkillRepository;
import com.osroyale.content.tradingpost.TradingPost;
import com.osroyale.content.triviabot.TriviaBot;
import com.osroyale.content.wintertodt.Wintertodt;
import com.osroyale.fs.cache.FileSystem;
import com.osroyale.fs.cache.decoder.*;
import com.osroyale.game.engine.GameEngine;
import com.osroyale.game.plugin.PluginManager;
import com.osroyale.game.service.*;
import com.osroyale.game.task.impl.ClanUpdateEvent;
import com.osroyale.game.task.impl.DoubleExperienceEvent;
import com.osroyale.game.task.impl.MessageEvent;
import com.osroyale.game.task.impl.PlayerSaveEvent;
import com.osroyale.game.world.World;
import com.osroyale.game.world.WorldType;
import com.osroyale.game.world.cronjobs.Jobs;
import com.osroyale.game.world.entity.combat.attack.listener.CombatListenerManager;
import com.osroyale.game.world.entity.combat.strategy.npc.boss.skotizo.SkotizoEvent;
import com.osroyale.game.world.entity.mob.npc.definition.NpcDefinition;
import com.osroyale.game.world.entity.mob.player.BannedPlayers;
import com.osroyale.game.world.entity.mob.player.IPBannedPlayers;
import com.osroyale.game.world.entity.mob.player.IPMutedPlayers;
import com.osroyale.game.world.entity.mob.player.profile.ProfileRepository;
import com.osroyale.game.world.items.ItemDefinition;
import com.osroyale.io.PacketListenerLoader;
import com.osroyale.net.LoginExecutorService;
import com.osroyale.net.discord.Discord;
import com.osroyale.net.discord.DiscordPlugin;
import com.osroyale.util.GameSaver;
import com.osroyale.util.Stopwatch;
import com.osroyale.util.parser.impl.*;
import dev.advo.fs.FileServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jire.tarnishps.OldToNew;
import org.jire.tarnishps.objectexamines.ObjectExamines;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import plugin.click.item.ClueScrollPlugin;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class OSRoyale {

    private static final Logger logger = LogManager.getLogger();

    public static final AtomicBoolean serverStarted = new AtomicBoolean(false);
    public static final Stopwatch UPTIME = Stopwatch.start();

    // services
    private static final StartupService startupService = new StartupService();
    private static final GameEngine gameService = new GameEngine();
    private static final NetworkService networkService = new NetworkService();

    private static final OSRoyale INSTANCE = new OSRoyale();

    private final LoginExecutorService loginExecutorService;

    private OSRoyale() {
        loginExecutorService = new LoginExecutorService(Runtime.getRuntime().availableProcessors());
    }

    private void processSequentialStatupTasks() {
        OldToNew.load();
        try {
            //object/region decoding must be done before parallel.
            new ObjectRemovalParser().run();
            final FileSystem fs = FileSystem.create("data/cache");
            new ObjectDefinitionDecoder(fs).run();
            new MapDefinitionDecoder(fs).run();
            new RegionDecoder(fs).run();
            new AnimationDefinitionDecoder(fs).run();
            CacheNpcDefinition.unpackConfig(fs.getArchive(FileSystem.CONFIG_ARCHIVE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ItemDefinition.createParser().run();
        NpcDefinition.createParser().run();
        ObjectExamines.loadObjectExamines();
        new CombatProjectileParser().run();
        CombatListenerManager.load();
        new NpcSpawnParser().run();
        new NpcDropParser().run();
        new NpcForceChatParser().run();
        new StoreParser().run();
        new GlobalObjectParser().run();
        ShootingStar.init();
        Wintertodt.init();
    }

    /**
     * Called after the sequential startup tasks, use this for faster startup.
     * Try not to use this method for tasks that rely on other tasks or you'll run into
     * issues.
     */
    private void processParallelStatupTasks() {
        startupService.submit(new PacketSizeParser());
        startupService.submit(new PacketListenerLoader());
        startupService.submit(TriviaBot::declare);
//        startupService.submit(PersonalStoreSaver::loadPayments);
        startupService.submit(ClanRepository::loadChannels);
        //  startupService.submit(GlobalRecords::load);
        startupService.submit(SkillRepository::load);
        startupService.submit(ProfileRepository::load);
        startupService.submit(ItemActionRepository::declare);
        startupService.submit(ClueScrollPlugin::declare);
        startupService.submit(MysteryBox::load);
        startupService.submit(Discord::start);
        startupService.submit(GameSaver::load);
        DiscordPlugin.startUp();
        startupService.submit(PreloadRepository::declare);
        startupService.submit(TradingPost::loadAllListings);
        startupService.submit(TradingPost::loadItemHistory);
        startupService.submit(TradingPost::loadRecentItemHistory);
        startupService.shutdown();
    }

    /**
     * Called when the game engine is running and all the startup tasks have finished loading
     */
    private void onStart() {
        if (WellOfGoodwill.isActive()) {
            World.schedule(new DoubleExperienceEvent());
        }

        World.schedule(new MessageEvent());
        World.schedule(new ClanUpdateEvent());
        World.schedule(new SkotizoEvent());
        World.schedule(new PlayerSaveEvent());
//        World.schedule(new BotStartupEvent());
        World.schedule(new BloodChestEvent());
        World.schedule(new LMSLobbyEvent());
        World.schedule(new LMSGameEvent());
        logger.info("Events have been scheduled");
    }

    public void start() throws Exception {
        if (Config.FORUM_INTEGRATION) {
            ForumService.start(); // used to check users logging in with website credentials

            if (Config.WORLD_TYPE == WorldType.LIVE) {
                PostgreService.start(); // used to start the postgres connection pool
                WebsitePlayerCountService.getInstance().startAsync(); // used to display player count on website
            }
        }

        new FileServer().start();

        logger.info("Tarnish is running (client version " + Config.CLIENT_VERSION + ")");
        logger.info(String.format("Game Engine=%s", Config.PARALLEL_GAME_ENGINE ? "Parallel" : "Sequential"));
        processSequentialStatupTasks();
        processParallelStatupTasks();

        startupService.awaitUntilFinished(5, TimeUnit.MINUTES);
        logger.info("Startup service finished");

        LMSLoadoutManager.load();

        PluginManager.load("plugin");

        gameService.startAsync().awaitRunning();
        logger.info("Game service started");

        onStart();

        Jobs.load();

        BannedPlayers.load();
        IPBannedPlayers.load();
        IPMutedPlayers.load();

        networkService.start(Config.SERVER_PORT);
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("shutting down server, initializing shutdown hook");
            World.save();
        }));

        try {
            INSTANCE.start();
        } catch (Throwable t) {
            logger.error("A problem has been encountered while starting the server.", t);
        }

    }

    public static OSRoyale getInstance() {
        return INSTANCE;
    }

    public static DateTime currentDateTime() {
        return new DateTime(timeZone());
    }

    public static DateTimeZone timeZone() {
        return DateTimeZone.UTC;
    }

    public LoginExecutorService getLoginExecutorService() {
        return this.loginExecutorService;
    }
}
