package com.digitstory.testapplication.Utils;

import com.digitstory.testapplication.Services.ContactWatchService;

public class ASingleton {

	private static volatile ASingleton instance;
	private static ContactWatchService cws;

	private ASingleton() {
	}

	public static ContactWatchService getInstance() {
		return cws;
	}

	public static void setInstance(ContactWatchService c){
		cws = c;
	}

}