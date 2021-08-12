package io.github.eatmyvenom.litematicin.utils;

import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_BREAK_BLOCKS;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_DELAY;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_FLUIDS;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_MAX_BLOCKS;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_PAPER;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_RANGE_X;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Y;
import static io.github.eatmyvenom.litematicin.LitematicaMixinMod.EASY_PLACE_MODE_RANGE_Z;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.materials.MaterialCache;
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager.PlacementPart;
import fi.dy.masa.litematica.schematic.placement.SubRegionPlacement.RequiredEnabled;
import fi.dy.masa.litematica.selection.Box;
import fi.dy.masa.litematica.util.EntityUtils;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.util.RayTraceUtils.RayTraceWrapper;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.LayerRange;
import fi.dy.masa.malilib.util.SubChunkPos;
import io.github.eatmyvenom.litematicin.utils.FacingDataStorage.FacingData;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EndRodBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SeaPickleBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.TripWireHookBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.world.World;

public class Printer {
	
    private static final List<PositionCache> positionCache = new ArrayList<>();

    private static FacingDataStorage facingDataStorage = new FacingDataStorage();
    
    /**
     * For now this function tries to equip the correct item for placing the block.
     * @param closest Not used.
     * @param mc {@code Minecraft} for gathering information and accessing the clientPlayer.
     * @param preference {@code BlockState} of how block should be after placing.
     * @param pos {@code BlockPos} of block you want to place.
     * @return true if correct item is in hand
     */
    public static boolean doSchematicWorldPickBlock(boolean closest, Minecraft mc, BlockState preference,
            BlockPos pos, ItemStack stack) {

        if (stack.isEmpty() == false) {
            PlayerInventory inv = mc.player.inventory;

            if (mc.player.abilities.instabuild) {
                // BlockEntity te = world.getBlockEntity(pos);

                // The creative mode pick block with NBT only works correctly
                // if the server world doesn't have a TileEntity in that position.
                // Otherwise it would try to write whatever that TE is into the picked
                // ItemStack.
                // if (GuiBase.isCtrlDown() && te != null && mc.level.isAir(pos)) {
                // ItemUtils.storeTEInStack(stack, te);
                // }

                // InventoryUtils.setPickedItemToHand(stack, mc);

                // NOTE: I dont know why we have to pick block in creative mode. You can simply
                // just set the block
                
                mc.gameMode.handleCreativeModeItemAdd(stack, 36 + inv.selected);

                return true;
            } else {
               
                int slot = inv.findSlotMatchingItem(stack);
                boolean shouldPick = inv.selected != slot;
                boolean canPick = (slot != -1) && slot < 36 && (EASY_PLACE_MODE_PAPER.getBooleanValue() ? slot < maxSlotId : true);

                if (shouldPick && canPick) {
                    InventoryUtils.setPickedItemToHand(stack, mc);
                    return true;
                    //return InteractionUtils.setPickedItemToHand(stack, mc);
                } else if (!shouldPick) {
                    return true;
                } else if (slot == -1 && Configs.Generic.PICK_BLOCK_SHULKERS.getBooleanValue()) {
                	slot = InventoryUtils.findSlotWithBoxWithItem(mc.player.inventoryMenu, stack, false);
                	if (slot != -1) {
                		ItemStack boxStack = mc.player.inventoryMenu.slots.get(slot).getItem();
                        InventoryUtils.setPickedItemToHand(boxStack, mc);
                        return true;
                	}
                }
            }
        }

        return false;
    }

    /**
     * Not supported.
     * @param mc {@code Minecraft}
     * @return null
     */
    public static ActionResultType doAccuratePlacePrinter(Minecraft mc) {
	return null;
    }
    
    // For printing delay
    private static long lastPlaced = new Date().getTime();
    private static Breaker breaker = new Breaker();
    
    // For height datapacks
    private static final int worldBottomY = 0;
    private static final int worldTopY = 256;
    
    // Paper anti-cheat values
    private static final int maxReachCreative = 6;
    private static final int maxReachSurvival = 6;
    private static final int maxSlotId = 9;
    private static final int maxDistance = 8;
    // This one is hard to determine, since paper starts to be suspicious when he receives more then 8 packets in a tick.
    // Value found by pure testing, and can probably be optimized even further
    private static final double minimumDelay = 0.1D;
    private static final int paperMaxInteractsPerFunctionCall = 1; // Otherwise to much packets at once

    // Water replacement
    public static BlockState waterReplacementBlock = Blocks.AIR.defaultBlockState();
    
    /**
     * Trying to place or break a block.
     * @param mc {@code Minecraft} for accessing the playerclient and managers...
     * @return {@code ActionResult} returns how well the placing/breaking went.
     */
    public static ActionResultType doPrinterAction(Minecraft mc) {
    	if (breaker.isBreakingBlock()) return ActionResultType.SUCCESS; // Don't place blocks while we're breaking one
    	if (new Date().getTime() < lastPlaced + 1000.0 * (EASY_PLACE_MODE_PAPER.getBooleanValue() ? minimumDelay : EASY_PLACE_MODE_DELAY.getDoubleValue())) return ActionResultType.PASS; // Check delay between blockplace's

    	// Configs
    	int rangeX = EASY_PLACE_MODE_RANGE_X.getIntegerValue();
        int rangeY = EASY_PLACE_MODE_RANGE_Y.getIntegerValue();
        int rangeZ = EASY_PLACE_MODE_RANGE_Z.getIntegerValue();
        int maxReach = Math.max(Math.max(rangeX,rangeY),rangeZ);
        boolean breakBlocks = EASY_PLACE_MODE_BREAK_BLOCKS.getBooleanValue();
        
        // Paper anti-cheat implementation
        if (EASY_PLACE_MODE_PAPER.getBooleanValue()) {
            if (mc.player.abilities.instabuild) {
                rangeX = maxReachCreative; 
                rangeY = maxReachCreative;
                rangeZ = maxReachCreative;
            } else {
                rangeX = maxReachSurvival; 
                rangeY = maxReachSurvival;
                rangeZ = maxReachSurvival;
            }
            maxReach = maxDistance;
            EASY_PLACE_MODE_DELAY.setDoubleValue(minimumDelay);
        }
        
    	
    	// Get the block the player is currently looking at
        RayTraceWrapper traceWrapper = RayTraceUtils.getGenericTrace(mc.level, mc.player, maxReach, true);
        if (traceWrapper == null) {
            return ActionResultType.FAIL;
        }
        BlockRayTraceResult trace = traceWrapper.getBlockHitResult();
        BlockPos tracePos = trace.getBlockPos();
        int posX = tracePos.getX();
        int posY = tracePos.getY();
        int posZ = tracePos.getZ();
        
        // Get all PlacementParts nearby the player's lookAtBlock
        SubChunkPos cpos = new SubChunkPos(tracePos);
        List<PlacementPart> list = DataManager.getSchematicPlacementManager().getAllPlacementsTouchingSubChunk(cpos);

        if (list.isEmpty()) {
            return ActionResultType.PASS;
        }
        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;
        int minX = 0;
        int minY = 0;
        int minZ = 0;

        // Setting min and max x,y,z
        boolean foundBox = false;
        for (PlacementPart part : list) {
            IntBoundingBox pbox = part.getBox();
            if (pbox.containsPos(tracePos)) {

                ImmutableMap<String, Box> boxes = part.getPlacement()
                        .getSubRegionBoxes(RequiredEnabled.PLACEMENT_ENABLED);

                for (Box box : boxes.values()) {

                    final int boxXMin = Math.min(box.getPos1().getX(), box.getPos2().getX());
                    final int boxYMin = Math.min(box.getPos1().getY(), box.getPos2().getY());
                    final int boxZMin = Math.min(box.getPos1().getZ(), box.getPos2().getZ());
                    final int boxXMax = Math.max(box.getPos1().getX(), box.getPos2().getX());
                    final int boxYMax = Math.max(box.getPos1().getY(), box.getPos2().getY());
                    final int boxZMax = Math.max(box.getPos1().getZ(), box.getPos2().getZ());

                    if (posX < boxXMin || posX > boxXMax || posY < boxYMin || posY > boxYMax || posZ < boxZMin
                            || posZ > boxZMax)
                        continue;
                    minX = boxXMin;
                    maxX = boxXMax;
                    minY = boxYMin;
                    maxY = boxYMax;
                    minZ = boxZMin;
                    maxZ = boxZMax;
                    foundBox = true;

                    break;
                }

                break;
            }
        }

        if (!foundBox) {
            return ActionResultType.PASS;
        }

        LayerRange range = DataManager.getRenderLayerRange(); // get renderingRange
        Direction[] facingSides = Direction.orderedByNearest(mc.player);
        Direction primaryFacing = facingSides[0];
        Direction horizontalFacing = primaryFacing; // For use in blocks with only horizontal rotation

        int index = 0;
        while (horizontalFacing.getAxis() == Direction.Axis.Y && index < facingSides.length) {
            horizontalFacing = facingSides[index++];
        }

        World world = SchematicWorldHandler.getSchematicWorld();

        /*
         * TODO: THIS IS REALLY BAD IN TERMS OF EFFICIENCY. I suggest using some form of
         * search with a built in datastructure first Maybe quadtree? (I dont know how
         * MC works)
         */

        int maxInteract = EASY_PLACE_MODE_PAPER.getBooleanValue() ? paperMaxInteractsPerFunctionCall : EASY_PLACE_MODE_MAX_BLOCKS.getIntegerValue();
        int interact = 0;
        boolean hasPicked = false;
        IFormattableTextComponent pickedBlock = null;

        // Ensure the positions are within the box and within range of the block the player is looking at
        int fromX = Math.max(posX - rangeX, minX);
        int fromY = Math.max(posY - rangeY, minY);
        int fromZ = Math.max(posZ - rangeZ, minZ);

        int toX = Math.min(posX + rangeX, maxX);
        int toY = Math.min(posY + rangeY, maxY);
        int toZ = Math.min(posZ + rangeZ, maxZ);

        // Ensure the Y is between the bottom and top of the world
        toY = Math.max(Math.min(toY,worldTopY),worldBottomY);
        fromY = Math.max(Math.min(fromY, worldTopY),worldBottomY); 

        // Ensure the positions are within the player's range
        fromX = Math.max(fromX,mc.player.blockPosition().getX()- rangeX);
        fromY = Math.max(fromY,mc.player.blockPosition().getY() - rangeY);
        fromZ = Math.max(fromZ,mc.player.blockPosition().getZ() - rangeZ);

        toX = Math.min(toX,mc.player.blockPosition().getX() + rangeX);
        toY = Math.min(toY,mc.player.blockPosition().getY() + rangeY);
        toZ = Math.min(toZ,mc.player.blockPosition().getZ() + rangeZ);
        
        
        for (int x = fromX; x <= toX; x++) {
            for (int y = fromY; y <= toY; y++) {
                for (int z = fromZ; z <= toZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    
                    BlockState stateSchematic = world.getBlockState(pos);
                    BlockState stateClient = mc.level.getBlockState(pos);
                    
                    if (stateSchematic == stateClient)
                        continue;
                    
                	// Offset to player
                    double dx = mc.player.getX() - x - 0.5;
                    double dy = mc.player.getY() - y - 0.5;
                    double dz = mc.player.getZ() - z - 0.5;
                    
                    // Another check if its within reach
                    if (dx * dx + dy * dy + dz * dz > maxReach * maxReach)
                    	continue;

                    // Paper anti-cheat
                    if (EASY_PLACE_MODE_PAPER.getBooleanValue()) {
                        double paperDx = mc.player.getX() - x;
                        double paperDy = mc.player.getEyeY() - y;
                        double paperDz = mc.player.getZ() - z;
                        double reachDistance = paperDx * paperDx + paperDy * paperDy + paperDz * paperDz;
                        
                        if (reachDistance > ((mc.player.abilities.instabuild) ? maxReachCreative * maxReachCreative : maxReachSurvival * maxReachSurvival))
                        	continue;
                    }
                    
                    if (range.isPositionWithinRange(pos) == false) // Check if block is rendered
                        continue;
                    
                    // Block breaking
                    if (breakBlocks && stateSchematic != null && !stateClient.isAir()) {
                        if (!stateClient.getBlock().getName().equals(stateSchematic.getBlock().getName()) && dx * dx + Math.pow(dy + 1.5,2) + dz * dz <= maxReach * maxReach) {
                            if (stateClient.getBlock() instanceof FlowingFluidBlock) {
                                if (stateClient.getValue(FlowingFluidBlock.LEVEL) == 0) {
                                    // Some manipulation with blockStates to reach the placement code
                                    stateClient = Blocks.AIR.defaultBlockState();
                                    // When air, should automatically continue;
                                    stateSchematic = waterReplacementBlock;
                                }
                            } else if (mc.player.abilities.instabuild) {
                        		mc.gameMode.startDestroyBlock(pos, Direction.DOWN);
                                interact++;

                                if (interact >= maxInteract) {
                                	lastPlaced = new Date().getTime();
                                    return ActionResultType.SUCCESS;
                                }
                        	} else if (stateClient.getDestroySpeed(mc.level, pos) != -1.0f) { // For survival, (don't break unbreakable blocks)
                        	    // When breakInstantly, a single attack is more then enough, (paper doesn't allow this)
                				if (stateClient.getDestroySpeed(mc.level, pos) == 0 && !EASY_PLACE_MODE_PAPER.getBooleanValue()) {
                				    mc.gameMode.startDestroyBlock(pos, Direction.DOWN);
                				    return ActionResultType.SUCCESS;
                				}
                            	breaker.startBreakingBlock(pos, mc);
                            	return ActionResultType.SUCCESS;
                        	}
                        }
                    }
                    
                    // Skip non source fluids & air
                    if (stateSchematic.isAir() 
                            || (stateSchematic.hasProperty(FlowingFluidBlock.LEVEL) && stateSchematic.getValue(FlowingFluidBlock.LEVEL) != 0)) 
                        continue;
                    
                    // If there's already a block of the same type, but it needs some more clicks (e.g. repeaters, half slabs, ...)
                    if (printerCheckCancel(stateSchematic, stateClient, mc.player)) {

                        /*
                         * Sometimes, blocks have other states like the delay on a repeater. So, this
                         * code clicks the block until the state is the same I don't know if Schematica
                         * does this too, I just did it because I work with a lot of redstone
                         */
                    	// stateClient.isAir() is already checked in the printCheckCancel
                        if (!mc.player.isShiftKeyDown() && !isPositionCached(pos, true)) {
                            Block cBlock = stateClient.getBlock();
                            Block sBlock = stateSchematic.getBlock();

                            if (cBlock.getName().equals(sBlock.getName())) {
                                Direction facingSchematic = fi.dy.masa.malilib.util.BlockUtils
                                        .getFirstPropertyFacingValue(stateSchematic);
                                Direction facingClient = fi.dy.masa.malilib.util.BlockUtils
                                        .getFirstPropertyFacingValue(stateClient);
                                
                                // If both block face the same direction
                                if (facingSchematic == facingClient) {
                                    int clickTimes = 0;
                                    Direction side = Direction.NORTH;
                                    
                                    // Check how much clicks each type of blocks need to be the same as the schematic
                                    if (sBlock instanceof RepeaterBlock) {
                                        int clientDelay = stateClient.getValue(RepeaterBlock.DELAY);
                                        int schematicDelay = stateSchematic.getValue(RepeaterBlock.DELAY);
                                        
                                        if (clientDelay != schematicDelay) {
                                        	clickTimes = schematicDelay - clientDelay;
                                        	if (clientDelay > schematicDelay) clickTimes += 4; // == schematicDelay + (4 - clientDelay); with 4-clientDelay the clickTime to zero delay
                                        }
                                        side = Direction.UP;
                                        
                                    } else if (sBlock instanceof ComparatorBlock) {
                                        if (stateSchematic.getValue(ComparatorBlock.MODE) 
                                        		!= stateClient.getValue(ComparatorBlock.MODE))
                                            clickTimes = 1;
                                        side = Direction.UP;
                                        
                                    } else if (sBlock instanceof LeverBlock) {
                                        if (stateSchematic.getValue(LeverBlock.POWERED) 
                                        		!= stateClient.getValue(LeverBlock.POWERED))
                                            clickTimes = 1;

                                        /*
                                         * I don't know if this direction code is needed. I am just doing it anyway so
                                         * it "make sense" to the server (I am emulating what the client does so
                                         * the server isn't confused)
                                         */
                                        if (stateClient.getValue(LeverBlock.FACE) == AttachFace.CEILING) {
                                            side = Direction.DOWN;
                                        } else if (stateClient.getValue(LeverBlock.FACE) == AttachFace.FLOOR) {
                                            side = Direction.UP;
                                        } else {
                                            side = stateClient.getValue(LeverBlock.FACING);
                                        }

                                    } else if (sBlock instanceof TrapDoorBlock) {
                                        if (stateSchematic.getMaterial() != Material.METAL 
                                        		&& stateSchematic.getValue(TrapDoorBlock.OPEN) != stateClient.getValue(TrapDoorBlock.OPEN))
                                            clickTimes = 1;
                                        
                                    } else if (sBlock instanceof FenceGateBlock) {
                                        if (stateSchematic.getValue(FenceGateBlock.OPEN) 
                                        		!= stateClient.getValue(FenceGateBlock.OPEN))
                                            clickTimes = 1;
                                        
                                    } else if (sBlock instanceof DoorBlock) {
                                        if (stateClient.getMaterial() != Material.METAL 
                                        		&& stateSchematic.getValue(DoorBlock.OPEN) != stateClient.getValue(DoorBlock.OPEN))
                                            clickTimes = 1;
                                        
                                    } else if (sBlock instanceof NoteBlock) {
                                        int note = stateClient.getValue(NoteBlock.NOTE);
                                        int targetNote = stateSchematic.getValue(NoteBlock.NOTE);
                                        if (note != targetNote) {

                                        	clickTimes = targetNote - note;
                                        	if (note > targetNote) clickTimes += 25; // == targetNote + (25-note); with (25-note) the amount of clicks to go back to start
                                        }
                                    }
                                    
                                    // Click on the block the amount of times calculated above
                                    for (int i = 0; i < clickTimes; i++) 
                                    {
                                        Hand hand = Hand.MAIN_HAND;

                                        Vector3d hitPos = new Vector3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

                                        BlockRayTraceResult hitResult = new BlockRayTraceResult(hitPos, side, pos, false);

                                        mc.gameMode.useItemOn(mc.player, mc.level, hand, hitResult);
                                        interact++;
                                        
                                        if (interact > maxInteract) {
                                        	/*
                                        	 * When the maxInteract is reached/exceeded && the block is clicked enough times
                                        	 * it reaches this line and returns without caching the block
                                        	 */
                                        	if (i == (clickTimes-1)) // If clicked enough times
                                        		cacheEasyPlacePosition(pos, true);
                                        	lastPlaced = new Date().getTime();
                                            return ActionResultType.SUCCESS;
                                        }
                                    }
 
                                    if (clickTimes > 0) {
                                        cacheEasyPlacePosition(pos, true);
                                    }
                                  
                                }
                            }
                        }
                        continue;
                    }
                    // If block is already placed (=> is already placed)
                    if (isPositionCached(pos, false)) continue;
                    
                    // If the player has the required item in his inventory or is in creative
                    ItemStack stack = MaterialCache.getInstance().getRequiredBuildItemForState(stateSchematic, world, pos);
                    
                    // The function above dus not take IWaterLoggable blocks in account
                    if (stateSchematic.getBlock() instanceof IWaterLoggable && stateSchematic.getValue(BlockStateProperties.WATERLOGGED) && stateClient.getBlock() == stateSchematic.getBlock())
                        stack = new ItemStack(Items.WATER_BUCKET);

                    if (stack.isEmpty() == false && (mc.player.abilities.instabuild || mc.player.inventory.findSlotMatchingItem(stack) != -1)) {
                        
                        Block sBlock = stateSchematic.getBlock();
                        
                        if (stateSchematic == stateClient)
                            continue;
                        
                        // If the item is a block
                        if (stack.getItem() instanceof BlockItem) {
                            // Block placing, when the correct block is already placed, but the state is incorrect, continue. (e.g. a powered rail that is not powered, we can't do anything about it, or the schematic is incomplete or the redstone isn't placed yet.
                            if (stateClient.getBlock() == stateSchematic.getBlock())
                                continue;
                            
                            // When gravity block, check if there's a block underneath
                            if (sBlock instanceof FallingBlock) {
                                BlockPos Offsetpos = new BlockPos(x, y-1, z);
                        		BlockState OffsetstateSchematic = world.getBlockState(Offsetpos);
                        		BlockState OffsetstateClient = mc.level.getBlockState(Offsetpos);
                        		
                                if (FallingBlock.isFree(OffsetstateClient) || (breakBlocks && !OffsetstateClient.getBlock().getName().equals(OffsetstateSchematic.getBlock().getName())) )
                                    continue;
                            } 
    
    
                            Direction facing = fi.dy.masa.malilib.util.BlockUtils
                                    .getFirstPropertyFacingValue(stateSchematic);
                            if (facing != null) {
                                FacingData facedata = facingDataStorage.getFacingData(stateSchematic);
                                if (!canPlaceFace(facedata, stateSchematic, mc.player, primaryFacing, horizontalFacing, facing))
                                    continue;
    
                                if ((stateSchematic.getBlock() instanceof DoorBlock
                                        && stateSchematic.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
                                        || (stateSchematic.getBlock() instanceof BedBlock
                                                && stateSchematic.getValue(BedBlock.PART) == BedPart.HEAD))
                                						continue;
                            }
                            // Exception for signs (edge case)
                            if (stateSchematic.getBlock() instanceof StandingSignBlock
                                    && !(stateSchematic.getBlock() instanceof WallSignBlock)) {
                                if ((MathHelper.floor((double) ((180.0F + mc.player.yRot) * 16.0F / 360.0F) + 0.5D)
                                        & 15) != stateSchematic.getValue(StandingSignBlock.ROTATION))
                                    continue;
    
                            }
                            
                            // We dont really need this. But I did it anyway so that I could experiment easily.
                            double offX = 0.5;
                            double offY = 0.5;
                            double offZ = 0.5;
    
                            Direction sideOrig = Direction.NORTH;
                            BlockPos npos = pos;
                            Direction side = applyPlacementFacing(stateSchematic, sideOrig, stateClient);
                            Block blockSchematic = stateSchematic.getBlock();
                            
                            // This should prevent the printer from placing torches and ... in water
                            if (!blockSchematic.canSurvive(stateSchematic, mc.level, pos)) continue;

                            if (blockSchematic instanceof HorizontalFaceBlock || blockSchematic instanceof TorchBlock
                                    || blockSchematic instanceof LadderBlock || blockSchematic instanceof TrapDoorBlock
                                    || blockSchematic instanceof TripWireHookBlock || blockSchematic instanceof StandingSignBlock
                                    || blockSchematic instanceof EndRodBlock) {
    
                                /*
                                 * Some blocks, especially wall mounted blocks must be placed on another for
                                 * directionality to work Basically, the block pos sent must be a "clicked"
                                 * block.
                                 */
                                int px = pos.getX();
                                int py = pos.getY();
                                int pz = pos.getZ();
                                
                                if (side == Direction.DOWN) {
                                    py += 1;
                                } else if (side == Direction.UP) {
                                    py += -1;
                                } else if (side == Direction.NORTH) {
                                    pz += 1;
                                } else if (side == Direction.SOUTH) {
                                    pz += -1;
                                } else if (side == Direction.EAST) {
                                    px += -1;
                                } else if (side == Direction.WEST) {
                                    px += 1;
                                }
    
                                npos = new BlockPos(px, py, pz);
    
                                BlockState clientStateItem = mc.level.getBlockState(npos);
    
                                if (clientStateItem == null || clientStateItem.isAir()) {
                                    if (!(blockSchematic instanceof TrapDoorBlock)) {
                                        continue;
                                    }
                                    BlockPos testPos;
    
                                    /*
                                     * Trapdoors are special. They can also be placed on top, or below another block
                                     */
                                    if (stateSchematic.getValue(TrapDoorBlock.HALF) == Half.TOP) {
                                        testPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
                                        side = Direction.DOWN;
                                    } else {
                                        testPos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
                                        side = Direction.UP;
                                    }
                                    BlockState clientStateItemTest = mc.level.getBlockState(testPos);
    
                                    if (clientStateItemTest == null || clientStateItemTest.isAir()) {
                                        BlockState schematicNItem = world.getBlockState(npos);
    
                                        BlockState schematicTItem = world.getBlockState(testPos);
    
                                        /*
                                         * If possible, it is always best to attatch the trapdoor to an actual block
                                         * that exists on the world But other times, it can't be helped
                                         */
                                        if ((schematicNItem != null && !schematicNItem.isAir())
                                                || (schematicTItem != null && !schematicTItem.isAir()))
                                            continue;
                                        npos = pos;
                                    } else
                                        npos = testPos;
    
                                    // If trapdoor is placed from top or bottom, directionality is decided by player
                                    // direction
                                    if (stateSchematic.getValue(TrapDoorBlock.FACING).getOpposite() != horizontalFacing)
                                        continue;
                                }
    
                            }
                            
                            // If player hasn't the correct item in his hand yet
                            // Depending on the maxInteracts, it tries to place the same block types in one function call
                            if (!hasPicked) {
                                if (doSchematicWorldPickBlock(true, mc, stateSchematic, pos, stack) == false) // When wrong item in hand
                                    return ActionResultType.FAIL;
                                hasPicked = true;
                                pickedBlock = stateSchematic.getBlock().getName();
                            } else if (pickedBlock != null && !pickedBlock.equals(stateSchematic.getBlock().getName()))
                                continue;
                            
                            Hand hand = EntityUtils.getUsedHandForItem(mc.player, stack);

                            // Go to next block if a wrong item is in the player's hand
                            // It will place the same block per function call
                            if (hand == null)
                                continue;
                            
                            Vector3d hitPos = new Vector3d(offX, offY, offZ);
                            // Carpet Accurate Placement protocol support, plus BlockSlab support
                            hitPos = applyHitVec(npos, stateSchematic, hitPos, side);
                            
                            BlockRayTraceResult hitResult = new BlockRayTraceResult(hitPos, side, npos, false);
                            
                            // System.out.printf("pos: %s side: %s, hit: %s\n", pos, side, hitPos);
                            // pos, side, hitPos
                            
                            ActionResultType actionResult = mc.gameMode.useItemOn(mc.player, mc.level, hand, hitResult);
                            
                            if (!actionResult.consumesAction()) 
                                continue;
                            
                            if (actionResult.shouldSwing())
                                   mc.player.swing(hand);
                            
                            // Ugly workaround, only cache when the block doesn't need to be waterlogged
                            if (!(stateSchematic.getBlock() instanceof IWaterLoggable && stateSchematic.getValue(BlockStateProperties.WATERLOGGED))) {
                                // Mark that this position has been handled (use the non-offset position that is checked above)
                                cacheEasyPlacePosition(pos, false);
                            }
                            interact++;
                            
                            // Place multiple slabs/pickles at once, since this is one block
                            // Disadvantage: you can exceed the maxInteract.
                            if (stateSchematic.getBlock() instanceof SlabBlock
                                    && stateSchematic.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
                                stateClient = mc.level.getBlockState(npos);

                                if (stateClient.getBlock() instanceof SlabBlock
                                        && stateClient.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
                                    side = applyPlacementFacing(stateSchematic, sideOrig, stateClient);
                                    hitResult = new BlockRayTraceResult(hitPos, side, npos, false);
                                    mc.gameMode.useItemOn(mc.player, mc.level, hand, hitResult);
                                    interact++;
                                }
                            }
                            else if (stateSchematic.getBlock() instanceof SeaPickleBlock
                                    && stateSchematic.getValue(SeaPickleBlock.PICKLES)>1) {
                                stateClient = mc.level.getBlockState(npos);
                                if (stateClient.getBlock() instanceof SeaPickleBlock
                                        && stateClient.getValue(SeaPickleBlock.PICKLES) < stateSchematic.getValue(SeaPickleBlock.PICKLES)) {
                                    side = applyPlacementFacing(stateSchematic, sideOrig, stateClient);
                                    hitResult = new BlockRayTraceResult(hitPos, side, npos, false);
                                    mc.gameMode.useItemOn(mc.player, mc.level, hand, hitResult);
                                    interact++;
                                }
                            }
                            
                        } else if (EASY_PLACE_MODE_FLUIDS.getBooleanValue()) { // If its an item
                            // TODO remove some of the duplicate code
                            // TODO support more items
                            ViewResult result = ViewResult.INVISIBLE;
                            
                            // Currently only water/lava blocks placement is supported
                            if (stateSchematic.getBlock() instanceof FlowingFluidBlock) {
                                
                                // Water can only be placed if the neighbor is a solid block -> not air and not a IWaterLoggable block
                                result = InteractionUtils.canSeeAndInteractWithBlock(pos, mc,
                                        (state) -> !state.isAir() && !state.hasProperty(BlockStateProperties.WATERLOGGED));
                                
                            }else if (stateSchematic.getBlock() instanceof IWaterLoggable) {
                                
                                // IWaterLoggable block only visible when neighbor is air.
                                result = InteractionUtils.canSeeAndInteractWithBlock(pos, mc,
                                        (state) -> state.isAir());
                            }
                            
                            if (result == ViewResult.INVISIBLE)
                                continue;
                            
                            // If player hasn't the correct item in his hand yet
                            // Depending on the maxInteracts, it tries to place the same block types in one function call
                            if (!hasPicked) {
                                if (doSchematicWorldPickBlock(true, mc, stateSchematic, pos, stack) == false) // When wrong item in hand
                                    return ActionResultType.FAIL;
                                hasPicked = true;
                                pickedBlock = stateSchematic.getBlock().getName();
                            } else if (pickedBlock != null && !pickedBlock.equals(stateSchematic.getBlock().getName()))
                                continue;
                            
                            Hand hand = EntityUtils.getUsedHandForItem(mc.player, stack);
                            
                            // Go to next block if a wrong item is in the player's hand
                            // It will place the same block per function call
                            if (hand == null)
                                continue;
                            
                            // Set player's rotation to fake rotation
                            float previousYaw = mc.player.yRot;
                            float previousPitch = mc.player.xRot;
                            
                            mc.player.yRot = result.yaw;
                            mc.player.xRot = result.pitch;

                            mc.player.connection.send(new CPlayerPacket.PositionRotationPacket(mc.player.getX(), mc.player.getY(), 
                                    mc.player.getZ(), mc.player.yRot, mc.player.xRot, mc.player.isOnGround()));
                            ActionResultType actionResult = mc.gameMode.useItem(mc.player, mc.level, hand);
                            
                            // Set rotation back to original
                            mc.player.yRot = previousYaw;
                            mc.player.xRot = previousPitch;
                            
                            if (!actionResult.consumesAction())
                                  continue;

                            if (actionResult.shouldSwing())
                               mc.player.swing(hand);
                           
                            // Mark that this position has been handled (use the non-offset position that is checked above)
                            cacheEasyPlacePosition(pos, false);
                            interact++;
                        }
                        
                        if (interact >= maxInteract) {
                            lastPlaced = new Date().getTime();
                            return ActionResultType.SUCCESS;
                        }
                    }
                }
            }
        }

        // If not exceeded maxInteract but placed a few blocks
        if (interact > 0) {
        	lastPlaced = new Date().getTime();
        	return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    /**
     * Checks if the block can be placed in the correct orientation if player is
     * facing a certain direction Dont place block if orientation will be wrong
     * @return true if face can be placed
     */
    private static boolean canPlaceFace(FacingData facedata, BlockState stateSchematic, PlayerEntity player,
            Direction primaryFacing, Direction horizontalFacing, Direction facing) {
        // facing != null is already checked before this function
    	if (facedata != null) {

            switch (facedata.type) {
            case 0: // All directions (ie, observers and pistons)
                if (facedata.isReversed) {
                    return facing.getOpposite() == primaryFacing;
                } else {
                    return facing == primaryFacing;
                }

            case 1: // Only Horizontal directions (ie, repeaters and comparators)
                if (facedata.isReversed) {
                    return facing.getOpposite() == horizontalFacing;
                } else {
                    return facing == horizontalFacing;
                }
            case 2: // Wall mountable, such as a lever, only use player direction if not on wall.
                return stateSchematic.getValue(HorizontalFaceBlock.FACE) == AttachFace.WALL
                        || facing == horizontalFacing;
            default: // Ignore rest -> TODO: Other blocks like anvils, etc...
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * Check whether there's already a block placed at that location and if it needs some extra clicks (e.g. Half slab, repeater, ...)
     * @param stateSchematic
     * @param stateClient
     * @param player
     * @return true if the block needs another click
     */
    private static boolean printerCheckCancel(BlockState stateSchematic, BlockState stateClient,
            PlayerEntity player) {
        Block blockSchematic = stateSchematic.getBlock();
        // TODO fully implement pickels, here it just check if it can be clicked
        if (blockSchematic instanceof SeaPickleBlock && stateSchematic.getValue(SeaPickleBlock.PICKLES) >1) {
            Block blockClient = stateClient.getBlock();

            if (blockClient instanceof SeaPickleBlock && stateClient.getValue(SeaPickleBlock.PICKLES) != stateSchematic.getValue(SeaPickleBlock.PICKLES)) {
                return blockSchematic != blockClient;
            }
        }
        else if (blockSchematic instanceof SlabBlock && stateSchematic.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
            Block blockClient = stateClient.getBlock();

            if (blockClient instanceof SlabBlock && stateClient.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
                return blockSchematic != blockClient;
            }
        }
        
        Block blockClient = stateClient.getBlock();
        if (blockClient instanceof SnowBlock && stateClient.getValue(SnowBlock.LAYERS) <3) {
                return false;
        }
        // If its air, the block doesn't need to be clicked again
        // This is a lot simpler than below. But slightly lacks functionality.
        if (stateClient.isAir() || stateClient.getBlock() instanceof FlowingFluidBlock
                || (stateSchematic.hasProperty(BlockStateProperties.WATERLOGGED) && stateClient.hasProperty(BlockStateProperties.WATERLOGGED)))
            return false;
        
        /*
         * if (trace.getType() != HitResult.Type.BLOCK) { return false; }
         */
        // BlockHitResult hitResult = (BlockHitResult) trace;
        // ItemPlacementContext ctx = new ItemPlacementContext(new
        // ItemUsageContext(player, Hand.MAIN_HAND, hitResult));

        // if (stateClient.canReplace(ctx) == false) {
        // return true;
        // }

        return true;
    }
    
    
    // Possible same as WorldUtils.applyCarpetProtocolHitVec
    /**
     * Apply hit vectors (used to be Carpet hit vec protocol, but I think it is
     * uneccessary now with orientation/states programmed in)
     * 
     * @param pos
     * @param state
     * @param hitVecIn
     * @return
     */
    public static Vector3d applyHitVec(BlockPos pos, BlockState state, Vector3d hitVecIn, Direction side) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        double dx = hitVecIn.x;
        double dy = hitVecIn.y;
        double dz = hitVecIn.z;
        Block block = state.getBlock();

        /*
         * I dont know if this is needed, just doing to mimick client According to the
         * MC protocol wiki, the protocol expects a 1 on a side that is clicked
         */
        if (side == Direction.UP) {
            dy = 1;
        } else if (side == Direction.DOWN) {
            dy = 0;
        } else if (side == Direction.EAST) {
            dx = 1;
        } else if (side == Direction.WEST) {
            dx = 0;
        } else if (side == Direction.SOUTH) {
            dz = 1;
        } else if (side == Direction.NORTH) {
            dz = 0;
        }

        if (block instanceof StairsBlock) {
            if (state.getValue(StairsBlock.HALF) == Half.TOP) {
                dy = 0.9;
            } else {
                dy = 0;
            }
        } else if (block instanceof SlabBlock && state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
            if (state.getValue(SlabBlock.TYPE) == SlabType.TOP) {
                dy = 0.9;
            } else {
                dy = 0;
            }
        } else if (block instanceof TrapDoorBlock) {
            if (state.getValue(TrapDoorBlock.HALF) == Half.TOP) {
                dy = 0.9;
            } else {
                dy = 0;
            }
        }
        return new Vector3d(x + dx, y + dy, z + dz);
    }

    /**
     * Gets the direction necessary to build the block oriented correctly. 
     * TODO: Need a better way to do this.
     */
    private static Direction applyPlacementFacing(BlockState stateSchematic, Direction side, BlockState stateClient) {
        Block blockSchematic = stateSchematic.getBlock();
        Block blockClient = stateClient.getBlock();

        if (blockSchematic instanceof SlabBlock) {
            if (stateSchematic.getValue(SlabBlock.TYPE) == SlabType.DOUBLE && blockClient instanceof SlabBlock
                    && stateClient.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
                if (stateClient.getValue(SlabBlock.TYPE) == SlabType.TOP) {
                    return Direction.DOWN;
                } else {
                    return Direction.UP;
                }
            }
            // Single slab
            else {
                return Direction.NORTH;
            }
        } else if (/*blockSchematic instanceof LogBlock ||*/ blockSchematic instanceof RotatedPillarBlock) {
            Direction.Axis axis = stateSchematic.getValue(RotatedPillarBlock.AXIS);
            // Logs and pillars only have 3 directions that are important
            if (axis == Direction.Axis.X) {
                return Direction.WEST;
            } else if (axis == Direction.Axis.Y) {
                return Direction.DOWN;
            } else if (axis == Direction.Axis.Z) {
                return Direction.NORTH;
            }

        } else if (blockSchematic instanceof WallSignBlock) {
            return stateSchematic.getValue(WallSignBlock.FACING);
        } else if (blockSchematic instanceof StandingSignBlock) {
            return Direction.UP;
        } else if (blockSchematic instanceof HorizontalFaceBlock) {
            AttachFace location = stateSchematic.getValue(HorizontalFaceBlock.FACE);
            if (location == AttachFace.FLOOR) {
                return Direction.UP;
            } else if (location == AttachFace.CEILING) {
                return Direction.DOWN;
            } else {
                return stateSchematic.getValue(HorizontalFaceBlock.FACING);

            }

        } else if (blockSchematic instanceof HopperBlock) {
            return stateSchematic.getValue(HopperBlock.FACING).getOpposite();
        } else if (blockSchematic instanceof TorchBlock) {

            if (blockSchematic instanceof WallTorchBlock) {
                return stateSchematic.getValue(WallTorchBlock.FACING);
            } else if (blockSchematic instanceof RedstoneWallTorchBlock) {
                return stateSchematic.getValue(RedstoneWallTorchBlock.FACING);
            } else {
                return Direction.UP;
            }
        } else if (blockSchematic instanceof LadderBlock) {
            return stateSchematic.getValue(LadderBlock.FACING);
        } else if (blockSchematic instanceof TrapDoorBlock) {
            return stateSchematic.getValue(TrapDoorBlock.FACING);
        } else if (blockSchematic instanceof TripWireHookBlock) {
            return stateSchematic.getValue(TripWireHookBlock.FACING);
        } else if (blockSchematic instanceof EndRodBlock) {
            return stateSchematic.getValue(EndRodBlock.FACING);
        }

        // TODO: Add more for other blocks
        return side;
    }

    /**
     * 
     * @param pos
     * @param useClicked
     * @return true when the {@code pos} is cached (if not {@code useClicked}, or if the block is cached and the block is a clickable one (repeater, ...)
     */
    public static boolean isPositionCached(BlockPos pos, boolean useClicked) {
        long currentTime = System.nanoTime();
        boolean cached = false;

        for (int i = 0; i < positionCache.size(); ++i) {
            PositionCache val = positionCache.get(i);
            boolean expired = val.hasExpired(currentTime);

            if (expired) {
                positionCache.remove(i);
                --i;
            } else if (val.getPos().equals(pos)) {

                // Item placement and "using"/"clicking" (changing delay for repeaters) are
                // diffferent
                if (!useClicked || val.hasClicked) {
                    cached = true;
                }

                // Keep checking and removing old entries if there are a fair amount
                if (positionCache.size() < 16) {
                    break;
                }
            }
        }

        return cached;
    }

    /**
     * Cache a placed block for an amount of time.
     * @param pos
     * @param useClicked
     */
    private static void cacheEasyPlacePosition(BlockPos pos, boolean useClicked) {
        PositionCache item = new PositionCache(pos, System.nanoTime(), useClicked ? 1000000000 : 2000000000);
        // TODO: Create a separate cache for clickable items, as this just makes
        // duplicates
        if (useClicked)
            item.hasClicked = true;
        positionCache.add(item);
    }

    public static class PositionCache {
        private final BlockPos pos;
        private final long time;
        private final long timeout;
        public boolean hasClicked = false;

        private PositionCache(BlockPos pos, long time, long timeout) {
            this.pos = pos;
            this.time = time;
            this.timeout = timeout;
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public boolean hasExpired(long currentTime) {
            return currentTime - this.time > this.timeout;
        }
    }
}
