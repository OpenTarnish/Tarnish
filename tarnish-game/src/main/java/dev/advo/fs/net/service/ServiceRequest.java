package dev.advo.fs.net.service;

public final class ServiceRequest {

	private final int id;

	public ServiceRequest(int id) {
		this.id = id;
	}

	public boolean invalid() {
		return id != 0;
	}
}
