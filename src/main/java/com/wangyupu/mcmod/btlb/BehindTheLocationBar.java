package com.wangyupu.mcmod.btlb;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BehindTheLocationBar implements ModInitializer {
	public static final String MOD_ID = "behind-the-location-bar";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		Command.initCommands();
	}
}