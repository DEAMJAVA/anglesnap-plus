package me.contaria.anglesnap.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.contaria.anglesnap.AngleEntry;
import me.contaria.anglesnap.AngleSnap;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {
    @Shadow
    private double lastHandleMovementTime;

    @Unique
    private double lastCursorMoveTime;
    @Unique
    private double lastSnapAngleTime;

    @WrapOperation(
            method = "turnPlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"
            )
    )
    private void snapToAngle(LocalPlayer player, double cursorDeltaX, double cursorDeltaY, Operation<Void> original) {
        if (this.lastSnapAngleTime + AngleSnap.CONFIG.snapLock.getValue() > this.lastHandleMovementTime) {
            cursorDeltaX = 0.0;
            cursorDeltaY = 0.0;
        }

        original.call(player, cursorDeltaX, cursorDeltaY);

        if (cursorDeltaX != 0.0 || cursorDeltaY != 0.0) {
            this.lastCursorMoveTime = this.lastHandleMovementTime;
        }
        this.snapToAngle(player);
    }

    @Unique
    private void snapToAngle(LocalPlayer player) {
        if (!(AngleSnap.shouldRenderOverlay() && AngleSnap.CONFIG.snapToAngle.getValue())) {
            return;
        }
        // don't snap to angle if mouse was in motion within snapDelay
        if (this.lastCursorMoveTime + AngleSnap.CONFIG.snapDelay.getValue() >= this.lastHandleMovementTime) {
            return;
        }

        AngleEntry closestAngle = null;
        float closestDistance = AngleSnap.CONFIG.snapDistance.getValue();
        for (AngleEntry angle : AngleSnap.CONFIG.getAngles()) {
            float distance = angle.getDistance(player.getYRot(), player.getXRot());
            if (distance < closestDistance) {
                closestAngle = angle;
                closestDistance = distance;
            }
        }
        if (closestAngle != null && closestDistance > 0.0f) {
            this.lastSnapAngleTime = this.lastHandleMovementTime;
            closestAngle.snap();
        }
    }
}
