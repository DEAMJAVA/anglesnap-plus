package me.contaria.anglesnap.mixin;

import me.contaria.anglesnap.AngleSnap;
import me.contaria.anglesnap.gui.camerasnap.CameraSnapScreen;
import me.contaria.anglesnap.gui.screen.AngleSnapScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public abstract void setScreenAndShow(Screen screen);

    @Shadow
    @Final
    public Gui gui;

    @Inject(
            method = "handleKeybinds",
            at = @At("TAIL")
    )
    private void openMenu(CallbackInfo ci) {
        while (AngleSnap.openMenu.consumeClick()) {
            if (AngleSnap.CONFIG.hasAngles() && this.gui.screen() == null) {
                this.setScreenAndShow(AngleSnapScreen.create(null));
            }
        }
        while (AngleSnap.cameraPositions.consumeClick()) {
            if (AngleSnap.currentCameraPos == null) {
                if (AngleSnap.CONFIG.hasCameraPositions() && this.gui.screen() == null) {
                    this.setScreenAndShow(CameraSnapScreen.create(null));
                }
            } else {
                AngleSnap.currentCameraPos = null;
            }
        }
    }
}
