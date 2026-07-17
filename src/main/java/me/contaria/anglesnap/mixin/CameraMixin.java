package me.contaria.anglesnap.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.contaria.anglesnap.AngleSnap;
import me.contaria.anglesnap.CameraPosEntry;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Inject(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z",
                    shift = At.Shift.AFTER
            )
    )
    private void modifyThirdPerson(float partialTicks, CallbackInfo ci) {
        this.detached = this.detached || AngleSnap.currentCameraPos != null;
    }

    @ModifyExpressionValue(
            method = "alignWithEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/CameraType;isMirrored()Z"
            )
    )
    private boolean modifyInverseView(boolean inverseView) {
        return inverseView && AngleSnap.currentCameraPos == null;
    }

    @Inject(
            method = "alignWithEntity",
            at = @At("TAIL")
    )
    private void modifyCameraPosition(float partialTicks, CallbackInfo ci) {
        CameraPosEntry entry = AngleSnap.currentCameraPos;
        if (entry != null) {
            this.setPosition(entry.x, entry.y, entry.z);
        }
    }
}
