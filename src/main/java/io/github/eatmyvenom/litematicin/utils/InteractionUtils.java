package io.github.eatmyvenom.litematicin.utils;

import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class InteractionUtils {
    
    /**
     * Check if an item can be used at the given {@code BlockPos}.
     * @param pos
     * @param mc
     * @return
     */
    public static ViewResult canSeeAndInteractWithBlock(BlockPos pos, Minecraft mc, Predicate<BlockState> statesToAccept) {
        Direction[] possibleDirections = Direction.values();
        
        for (int i = 0; i < possibleDirections.length; i++) {
            Vector3i vec = possibleDirections[i].getNormal();
            BlockState state = mc.level.getBlockState(pos.offset(vec));
            
            // You can't place water on air or a waterloggen block
            if (!statesToAccept.test(state)) continue;
            
            ViewResult result = isVisible(mc, pos, possibleDirections[i]);
            
            if (result == ViewResult.VISIBLE) return result;
                
        }
        
        return ViewResult.INVISIBLE;
    }
    
    /**
     * Returns the needed yaw & pitch to look at a blockFace.
     * @param me
     * @param pos
     * @param blockFace
     * @return
     */
    private static Rotation getNeededRotation(PlayerEntity me, BlockPos pos, Direction blockFace)
    {
        Vector3d posD = Vector3d.atCenterOf(pos);
        Vector3d to = posD.add(Vector3d.atLowerCornerOf(blockFace.getNormal()).scale(0.5d));
        
        double dirx = me.getX() - to.x();
        double diry = me.getEyeY() - to.y();
        double dirz = me.getZ() - to.z();

        double len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;

        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);
        
        //to degree
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;

        yaw += 90d;
        
        return new Rotation((float)yaw, (float)pitch, (float)len);
    }
    
    /**
     * Check if a blockFace at {@code BlockPos} is visible for the player.
     * @param player
     * @param toSee
     * @param blockFace
     * @return
     */
    private static ViewResult isVisible(Minecraft mc, BlockPos toSee, Direction blockFace) {
        final ClientPlayerEntity player = mc.player;
        Rotation rotation = getNeededRotation(player, toSee, blockFace);
        
        float tickDelta = mc.getFrameTime();
        double maxDist = rotation.maxDist + 0.5f;
        
        Vector3d vec3d = player.getEyePosition(tickDelta);
        Vector3d vec3d2 = getRotationVector(rotation.pitch, rotation.yaw);
        Vector3d vec3d3 = vec3d.add(vec3d2.x * maxDist, vec3d2.y * maxDist, vec3d2.z * maxDist);
        RayTraceResult result = mc.level.clip(new RayTraceContext(vec3d, vec3d3,
                RayTraceContext.BlockMode.OUTLINE, 
                RayTraceContext.FluidMode.ANY, player));
        
        if (result.getType() == Type.BLOCK 
                && !(result.getLocation().distanceToSqr(player.getX(), player.getEyeY(), player.getZ()) < rotation.maxDist * rotation.maxDist)) { // If there's a block between the player and the location
            ViewResult viewResult = ViewResult.VISIBLE;
            viewResult.pitch = rotation.pitch;
            viewResult.yaw = rotation.yaw;
            
            return viewResult;
        }
        return ViewResult.INVISIBLE;
    }
    
    /**
     * An overridden function from Minecraft. This original function was protected, so we couldn't use it.
     * @param pitch
     * @param yaw
     * @return
     */
    private static Vector3d getRotationVector(float pitch, float yaw) {
        float f = pitch * 0.017453292F;
        float g = -yaw * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vector3d((double)(i * j), (double)(-k), (double)(h * j));
    }
    
    /**
     * A class created to carry variables between functions.
     *
     */
    private static class Rotation {
        public float yaw, pitch, maxDist;
        
        public Rotation(float yaw, float pitch, float maxDist) {
            this.pitch = pitch;
            this.yaw = yaw;
            this.maxDist = maxDist;
        }
    }

}
