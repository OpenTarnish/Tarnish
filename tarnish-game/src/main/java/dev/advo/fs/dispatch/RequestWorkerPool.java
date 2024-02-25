package dev.advo.fs.dispatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.advo.fs.fs.IndexedFileSystem;


/**
 * A class which manages the pool of request workers.
 * @author Graham
 */
public final class RequestWorkerPool {
	
	/**
	 * The number of threads per request type.
	 */
	private static final int THREADS_PER_REQUEST_TYPE = Runtime.getRuntime().availableProcessors();
		
	/**
	 * The executor service.
	 */
	private final ExecutorService service;
	
	/**
	 * A list of request workers.
	 */
	private final List<OnDemandRequestWorker> workers = new ArrayList<OnDemandRequestWorker>();
	
	/**
	 * The request worker pool.
	 */
	public RequestWorkerPool() {
		int totalThreads = THREADS_PER_REQUEST_TYPE;
		service = Executors.newFixedThreadPool(totalThreads);
	}

	/**
	 * Starts the threads in the pool.
	 * @throws Exception if the file system cannot be created.
	 */
	public void start() throws Exception {
		File base = new File("./data/cache/");

		for (int i = 0; i < THREADS_PER_REQUEST_TYPE; i++) {
			workers.add(new OnDemandRequestWorker(new IndexedFileSystem(base, true)));
		}
		
		for (OnDemandRequestWorker worker : workers) {
			service.submit(worker);
		}
	}

}
