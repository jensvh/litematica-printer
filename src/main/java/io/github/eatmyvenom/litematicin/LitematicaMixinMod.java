package io.github.eatmyvenom.litematicin;

import com.google.common.collect.ImmutableList;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigString;
import io.github.eatmyvenom.litematicin.utils.Printer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("litematica-printer")
public class LitematicaMixinMod {

	public static final ConfigInteger EASY_PLACE_MODE_RANGE_X      	= new ConfigInteger("easyPlaceModeRangeX", 3, 0, 1024, "X Range for EasyPlace");
	public static final ConfigInteger EASY_PLACE_MODE_RANGE_Y      	= new ConfigInteger("easyPlaceModeRangeY", 3, 0, 1024, "Y Range for EasyPlace");
	public static final ConfigInteger EASY_PLACE_MODE_RANGE_Z      	= new ConfigInteger("easyPlaceModeRangeZ", 3, 0, 1024, "Z Range for EasyPlace");
	public static final ConfigInteger EASY_PLACE_MODE_MAX_BLOCKS   	= new ConfigInteger("easyPlaceModeMaxBlocks", 3, 1, 1000000, "Max block interactions per cycle");
	public static final ConfigBoolean EASY_PLACE_MODE_BREAK_BLOCKS 	= new ConfigBoolean("easyPlaceModeBreakBlocks", false, "Automatically breaks blocks.");
	public static final ConfigDouble  EASY_PLACE_MODE_DELAY		   	= new ConfigDouble( "easyPlaceModeDelay", 0.2, 0.0, 1.0, "Delay between printing blocks.\nDo not set to 0 if you are playing on a server.");
    public static final ConfigBoolean EASY_PLACE_MODE_PAPER			= new ConfigBoolean("easyPlaceModePaper", false, "Enable this feature to bypass the built-in papers anti-cheat. This will make the range stricter, delay lower and only pick blocks from the hotbar.");
    public static final ConfigBoolean EASY_PLACE_MODE_FLUIDS        = new ConfigBoolean("easyPlaceModeFluids", false, "Enable for placing fluid(water/lava) sources or waterlogged blocks. Be aware, this functions uses \"Fake rotations\", this can be seen as hacking!");
    public static final ConfigString  EASY_PLACE_MODE_REPLACE_FLUIDS= new ConfigString("easyPlaceModeReplaceFluids", "none", "To enable, type the name of the block which should be placed to remove excessive fluid sources.");
    
    public static final ImmutableList<IConfigBase> betterList = ImmutableList.<IConfigBase>builder()
			.addAll(Configs.Generic.OPTIONS)
			.add(EASY_PLACE_MODE_RANGE_X)
			.add(EASY_PLACE_MODE_RANGE_Y)
			.add(EASY_PLACE_MODE_RANGE_Z)
			.add(EASY_PLACE_MODE_MAX_BLOCKS)
			.add(EASY_PLACE_MODE_BREAK_BLOCKS)
			.add(EASY_PLACE_MODE_DELAY)
			.add(EASY_PLACE_MODE_PAPER)
			.add(EASY_PLACE_MODE_FLUIDS)
			.add(EASY_PLACE_MODE_REPLACE_FLUIDS)
			.build();
    
    public LitematicaMixinMod()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(final FMLClientSetupEvent event)
    {
		System.out.println("YeeFuckinHaw");
		EASY_PLACE_MODE_REPLACE_FLUIDS.setValueChangeCallback((config) -> {
		    String name = config.getStringValue();
		    
	        if (name.isEmpty() || name.equals("none")) {
	            Printer.waterReplacementBlock = Blocks.AIR.defaultBlockState();
	            return;
	        }
	        Block block = Registry.BLOCK.get(new ResourceLocation(name));

	        if (block != null && block.defaultBlockState().getMaterial().isSolid()) { 
	            Printer.waterReplacementBlock = block.defaultBlockState();
	            return;
	        }
	        
	        Printer.waterReplacementBlock = Blocks.AIR.defaultBlockState();
		});
	}
}
