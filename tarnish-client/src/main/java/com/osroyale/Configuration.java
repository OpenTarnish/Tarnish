package com.osroyale;

/**
 * Handles all configuration settings for the client.
 *
 * @author Daniel
 * @author Jire
 */
public final class Configuration {

    public static final String LIVE_GAME_ADDRESS = "127.0.0.1";
    public static final int LIVE_GAME_PORT = 43594;

    public static final String LIVE_CACHE_ADDRESS = "127.0.0.1";
    public static final int LIVE_CACHE_PORT = 43595;

    /**
     * The IP address client will be connecting to.
     */
    public static final Connection CONNECTION = Connection.ECONOMY;

    /**
     * State of client being in debug mode.
     */
    public static boolean DEBUG_MODE = false;

    /**
     * Display client data.
     */
    static boolean CLIENT_DATA = false;

    /**
     * Debug the interfaces.
     */
    static boolean DUMP_INTERFACES = false;

    /**
     * State of client enabling RSA encryption.
     */
    static boolean ENABLE_RSA = true;

    /**
     * The current NPC bits.
     */
    static final int NPC_BITS = 16;

    /**
     * The current version of the client.
     */
    public static final int CLIENT_VERSION = 6;

    /**
     * The name of the client.
     */
    public final static String NAME = "Tarnish";

    /**
     * The cache file name.
     */
    public final static String CACHE_NAME = ".tarnish";

    public static final String SPRITE_FILE_NAME = "main_file_sprites";

    /**
     * The character folder path.
     */
    static final String CHAR_PATH = Utility.findcachedir() + "Character";

    /**
     * All the announcements which will be displayed on the loginscreen.
     */
    public final static String[] ANNOUNCEMENT = {
            "Welcome to " + NAME,
    };

    /**
     * Whether to use Jire SwiftFUP update server.
     */
    public static final boolean USE_UPDATE_SERVER = true;
    public static final String UPDATE_SERVER_IP = CONNECTION.getUpdateAddress();
    public static final int UPDATE_SERVER_PORT = CONNECTION.getUpdatePort();

}
