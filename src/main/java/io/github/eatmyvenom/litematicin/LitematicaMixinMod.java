package io.github.eatmyvenom.litematicin;

import com.google.common.collect.ImmutableList;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import net.fabricmc.api.ModInitializer;

/**
 * Updated:
 * - Make code more readable
 * - remove the hardcoded maxReach of block breaking
 * - Make compatible with all the features of multiple versions of Litematica but of same minecraft version (adaptive betterlist)
 * 
 * @author Ikke
 *
 */
public class LitematicaMixinMod implements ModInitializer {

	public static final ConfigInteger EASY_PLACE_MODE_RANGE_X      	= new ConfigInteger("easyPlaceModeRangeX", 3, 0, 1024, "X Range for EasyPlace");
	public static final ConfigInteger EASY_PLACE_MODE_RANGE_Y      	= new ConfigInteger("easyPlaceModeRangeY", 3, 0, 1024, "Y Range for EasyPlace");
	public static final ConfigInteger EASY_PLACE_MODE_RANGE_Z      	= new ConfigInteger("easyPlaceModeRangeZ", 3, 0, 1024, "Z Range for EasyPlace");
	public static final ConfigInteger EASY_PLACE_MODE_MAX_BLOCKS   	= new ConfigInteger("easyPlaceModeMaxBlocks", 3, 1, 1000000, "Max block interactions per cycle");
	public static final ConfigBoolean EASY_PLACE_MODE_BREAK_BLOCKS 	= new ConfigBoolean("easyPlaceModeBreakBlocks", false, "Automatically breaks blocks.");
	public static final ConfigDouble  EASY_PLACE_MODE_DELAY		   	= new ConfigDouble( "easyPlaceModeDelay", 0.2, 0.0, 1.0, "Delay between printing blocks.\nDo not set to 0 if you are playing on a server.");
    public static final ConfigBoolean EASY_PLACE_MODE_HOTBAR_ONLY	= new ConfigBoolean("easyPlaceModeHotbarOnly", false, "Only place blocks from your hotbar.");
    
    public static final ImmutableList<IConfigBase> betterList = ImmutableList.<IConfigBase>builder()
			.addAll(Configs.Generic.OPTIONS)
			.add(EASY_PLACE_MODE_RANGE_X)
			.add(EASY_PLACE_MODE_RANGE_Y)
			.add(EASY_PLACE_MODE_RANGE_Z)
			.add(EASY_PLACE_MODE_MAX_BLOCKS)
			.add(EASY_PLACE_MODE_BREAK_BLOCKS)
			.add(EASY_PLACE_MODE_DELAY)
			.add(EASY_PLACE_MODE_HOTBAR_ONLY)
			.build();
    
	@Override
	public void onInitialize() {
		System.out.println("YeeFuckinHaw");
	}
}
