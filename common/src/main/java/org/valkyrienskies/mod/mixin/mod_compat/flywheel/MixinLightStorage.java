package org.valkyrienskies.mod.mixin.mod_compat.flywheel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.engine_room.flywheel.backend.engine.LightDataCollector;
import dev.engine_room.flywheel.backend.engine.LightStorage;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.mod.compat.VSRenderer;
import org.valkyrienskies.mod.compat.sodium.SodiumCompat;
import org.valkyrienskies.mod.compat.sodium.light.VsWorldFromShipLightStorage;
import org.valkyrienskies.mod.mixin.ValkyrienCommonMixinConfigPlugin;

@Mixin(LightStorage.class)
public class MixinLightStorage {
    @WrapOperation(
        method = "collectSection",
        at = @At(value = "INVOKE",
            target = "Ldev/engine_room/flywheel/backend/engine/LightDataCollector;collectSection(JJ)V"),
        remap = false
    )
    private void collectSectionIfDynamicLight(LightDataCollector lightDataCollector, long ptr, long section, Operation<Void> original){
        original.call(lightDataCollector, ptr, section);
        if (ValkyrienCommonMixinConfigPlugin.getVSRenderer() == VSRenderer.SODIUM) {
            long sectionShipToWorld = SodiumCompat.getWorldFromShipStorage().copySectionLight(section);
            if (sectionShipToWorld == MemoryUtil.NULL) return;
            for (long i = VsWorldFromShipLightStorage.LIGHT_START_BYTES; i < VsWorldFromShipLightStorage.SECTION_SIZE_BYTES; i++) {
                int lightFromShip = (MemoryUtil.memGetByte(sectionShipToWorld + i) & 0x0f);
                int lightOriginal = (MemoryUtil.memGetByte(ptr + i) & 0xf0);
                if (lightFromShip != 0) MemoryUtil.memPutByte(ptr + i, (byte) (lightFromShip | lightOriginal));
            }
        }
    }
}
