package io.github.eatmyvenom.litematicin.utils;

import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.malilib.event.TickHandler;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.interfaces.IClientTickHandler;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * The breaking needs to be done every tick, since the WorldUtils.easyPlaceOnUseTick (which calls our Printer)
 * is called multiple times per tick we cannot break blocks through that method. Or the speed will be twice the
 * normal speed and detectable by anti-cheats.
 * 
 *
 */
public class Breaker implements IClientTickHandler {
	
	private boolean breakingBlock;
	private BlockPos pos;
	
	/**
	 * Initialize the breaker and register this as a ClientTickHandler.
	 */
	public Breaker() {
		this.breakingBlock = false;
		this.pos = null;
		
		TickHandler.getInstance().registerClientTickHandler(this);
	}
	
	/**
	 * Start breaking a block at location {@code pos}.
	 * @param pos {@code BlockPos} of the block to break
	 * @param mc {@code MinecraftClient} for accessing data
	 */
	public void startBreakingBlock(BlockPos pos, Minecraft mc) {
		this.breakingBlock = true;
		this.pos = pos;
		// Check for best tool in inventory
		int bestSlotId = getBestItemSlotIdToMineBlock(mc, pos);
		// If slot isn't selected, change
		if (mc.player.inventory.selected != bestSlotId) {
			mc.player.inventory.selected = bestSlotId;
		}
		// Start breaking
		TickHandler.getInstance().registerClientTickHandler(this);
	}
	
	/**
	 * Check if we're still breaking a block.
	 * @return True if still breaking a block.
	 */
	public boolean isBreakingBlock() {
		return this.breakingBlock;
	}
	
	/**
	 * Get the best item slot id for mining a block at {@code blockToMine}. 
	 * This function will look for the fastest item you can mine that block with, 
	 * if there is non, it will return an item that cannot break.
	 * @param mc {@code MinecraftClient} for accessing data.
	 * @param blockToMine {@code BlockPos} of block to compare blockBreakingSpeeds with.
	 * @return slotId as an {@code Integer}
	 */
	private int getBestItemSlotIdToMineBlock(Minecraft mc, BlockPos blockToMine) {
		int bestSlot = 0;
		float bestSpeed = 0;
		BlockState state = mc.level.getBlockState(blockToMine);
		for (int i = 8; i >= 0; i--) {
			float speed = getBlockBreakingSpeed(state, mc, i);
			if ((speed > bestSpeed && speed > 1.0F)
					|| (speed >= bestSpeed && !mc.player.inventory.getItem(i).isDamageableItem())) {
				bestSlot = i;
				bestSpeed = speed;
			}
		}
		return bestSlot;
	}
	
	/**
	 * Get the blockBreakingSpeed of an item at inventorySlot with id {@code slotId} that mines the {@code block}.
	 * @param block {@code BlockState} of block the item needs to mine.
	 * @param mc {@code MinecraftClient} for accessing data.
	 * @param slotId id where item is in inventory.
	 * @return blockBreakingSpeed as a {@code Float}
	 */
	private float getBlockBreakingSpeed(BlockState block, Minecraft mc, int slotId) {
		float f = ((ItemStack)mc.player.inventory.items.get(slotId)).getDestroySpeed(block);
	    if (f > 1.0F) {
	       int i = EnchantmentHelper.getBlockEfficiency(mc.player);
	       ItemStack itemStack = mc.player.inventory.getSelected();
	       if (i > 0 && !itemStack.isEmpty()) {
	          f += (float)(i * i + 1);
	       }
	    }
	    return f;
	}
	/**
	 * Don't call this function, it's automatically called every tick by malilib.
	 */
	@Override
	public void onClientTick(Minecraft mc) {
		if (!isBreakingBlock()) return;
		if (mc.player == null) return;
		
		if (Hotkeys.EASY_PLACE_ACTIVATION.getKeybind().isKeybindHeld() &&
	            KeybindMulti.isKeyDown(KeybindMulti.getKeyCode(mc.options.keyUse))) { // Only continue mining while the correct keys are pressed
			Direction side = Direction.values()[0];
			
			if (mc.gameMode.continueDestroyBlock(pos, side)) {
				mc.particleEngine.crack(pos, side);
				mc.player.swing(Hand.MAIN_HAND);
			}
		}
		
		if (!mc.level.getBlockState(pos).isAir()) return; // If block isn't broken yet, dont stop
		
		// Stop breaking
		this.breakingBlock = false;
		mc.gameMode.stopDestroyBlock();
	}
	
}
