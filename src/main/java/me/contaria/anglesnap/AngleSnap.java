package me.contaria.anglesnap;

import com.mojang.blaze3d.vertex.PoseStack;
import me.contaria.anglesnap.config.AngleSnapConfig;
import me.contaria.anglesnap.renderer.IconFeatureRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AngleSnap implements ClientModInitializer {
    public static final String MOD_ID = "anglesnap";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final AngleSnapConfig CONFIG = new AngleSnapConfig();

    public static KeyMapping.Category keybindCategory;
    public static KeyMapping openMenu;
    public static KeyMapping openOverlay;
    public static KeyMapping cameraPositions;

    @Nullable
    public static CameraPosEntry currentCameraPos;

    @Override
    public void onInitializeClient() {
        FeatureRendererRegistry.register(IconFeatureRenderer.TYPE, IconFeatureRenderer::new);

        keybindCategory = KeyMapping.Category.register(AngleSnap.idOf("main"));

        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "anglesnap.key.openmenu",
                GLFW.GLFW_KEY_F6,
                keybindCategory
        ));
        openOverlay = KeyMappingHelper.registerKeyMapping(new ToggleKeyMapping(
                "anglesnap.key.openoverlay",
                GLFW.GLFW_KEY_F7,
                keybindCategory,
                () -> true,
                true
        ));
        cameraPositions = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "anglesnap.key.camerapositions",
                GLFW.GLFW_KEY_F8,
                keybindCategory
        ));

        LevelRenderEvents.END_MAIN.register(AngleSnap::renderOverlay);
        HudElementRegistry.addFirst(AngleSnap.idOf("overlay"), AngleSnap::renderHud);

        ClientPlayConnectionEvents.JOIN.register((networkHandler, packetSender, client) -> {
            if (client.hasSingleplayerServer()) {
                AngleSnap.CONFIG.loadAnglesAndCameraPositions(Objects.requireNonNull(client.getSingleplayerServer()).getWorldPath(LevelResource.ROOT).getParent().getFileName().toString(), AngleSnapConfig.AngleFolder.SINGLEPLAYER);
            } else if (Objects.requireNonNull(networkHandler.getServerData()).isRealm()) {
                AngleSnap.CONFIG.loadAnglesAndCameraPositions(networkHandler.getServerData().name, AngleSnapConfig.AngleFolder.REALMS);
            } else {
                AngleSnap.CONFIG.loadAnglesAndCameraPositions(networkHandler.getServerData().ip, AngleSnapConfig.AngleFolder.MULTIPLAYER);
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((networkHandler, client) -> AngleSnap.CONFIG.unloadAnglesAndCameraPositions());
    }

    public static boolean shouldRenderOverlay() {
        return openOverlay.isDown();
    }

    private static void renderHud(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        if (shouldRenderOverlay()) {
            if (AngleSnap.CONFIG.angleHud.getValue()) {
                renderAngleHud(context);
            }
        }
    }

    private static void renderOverlay(LevelRenderContext context) {
        if (shouldRenderOverlay()) {
            for (AngleEntry angle : AngleSnap.CONFIG.getAngles()) {
                renderMarker(context, angle, AngleSnap.CONFIG.markerScale.getValue(), AngleSnap.CONFIG.textScale.getValue());
            }
        }
    }

    private static void renderMarker(LevelRenderContext context, AngleEntry angle, float markerScale, float textScale) {
        markerScale = markerScale / 10.0f;
        textScale = textScale / 50.0f;

        Vector3f pos = Vec3.directionFromRotation(
                Mth.wrapDegrees(angle.pitch),
                Mth.wrapDegrees(angle.yaw + 180.0f)
        ).multiply(-1.0, 1.0, -1.0).toVector3f();
        Quaternionf rotation = context.gameRenderer().mainCamera().rotation();;

        drawIcon(context, pos, rotation, angle, markerScale);
        if (!angle.name.isEmpty()) {
            drawName(context, pos, rotation, angle, textScale);
        }
    }

    private static void drawIcon(LevelRenderContext context, Vector3f pos, Quaternionf rotation, AngleEntry angle, float scale) {
        if (scale == 0.0f) {
            return;
        }

        PoseStack poseStack = Objects.requireNonNull(context.poseStack());
        OrderedSubmitNodeCollector collector = Objects.requireNonNull(context.submitNodeCollector());

        poseStack.pushPose();
        poseStack.translate(pos.x(), pos.y(), pos.z());
        poseStack.mulPose(rotation);
        poseStack.scale(scale, -scale, scale);

        RenderType layer = RenderTypes.entityTranslucent(angle.getIcon());
        IconFeatureRenderer.Submit submit = new IconFeatureRenderer.Submit(
                new Matrix4f(poseStack.last().pose()),
                layer,
                angle.color
        );

        collector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, submit);

        poseStack.scale(1.0f / scale, 1.0f / -scale, 1.0f / scale);
        poseStack.popPose();
    }

    private static void drawName(LevelRenderContext context, Vector3f pos, Quaternionf rotation, AngleEntry angle, float scale) {
        if (scale == 0.0f || angle.name.isEmpty()) {
            return;
        }

        PoseStack poseStack = Objects.requireNonNull(context.poseStack());
        OrderedSubmitNodeCollector collector = Objects.requireNonNull(context.submitNodeCollector());

        poseStack.pushPose();
        poseStack.translate(pos.x(), pos.y(), pos.z());
        poseStack.mulPose(rotation);
        poseStack.scale(scale, -scale, scale);

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        float x = -font.width(angle.name) / 2.0f;
        int backgroundColor = (int) (client.options.getBackgroundOpacity(0.25f) * 255.0f) << 24;

        TextFeatureRenderer.Submit submit = new TextFeatureRenderer.Submit(
                new Matrix4f(poseStack.last().pose()),
                x, -15.0f,
                Component.literal(angle.name).getVisualOrderText(),
                false,
                Font.DisplayMode.SEE_THROUGH,
                15,
                CommonColors.WHITE,
                backgroundColor,
                0
        );

        collector.submitCustom(SubmitRenderPhases.AFTER_TERRAIN, submit);

        poseStack.scale(1.0f / scale, 1.0f / -scale, 1.0f / scale);
        poseStack.popPose();
    }

    private static void renderAngleHud(GuiGraphicsExtractor context) {
        Minecraft client = Minecraft.getInstance();
        if (client.getDebugOverlay().showDebugScreen() || client.player == null) {
            return;
        }

        Font textRenderer = client.font;
        String text = String.format("%.3f / %.3f", Mth.wrapDegrees(client.player.getYRot()), Mth.wrapDegrees(client.player.getXRot()));
        context.fill(5, 5, 5 + 2 + textRenderer.width(text) + 2, 5 + 2 + textRenderer.lineHeight + 2, -1873784752);
        context.text(textRenderer, text, 5 + 2 + 1, 5 + 2 + 1, -2039584, false);
    }

    public static boolean isInMultiplayer() {
        return Minecraft.getInstance().level != null && !Minecraft.getInstance().isLocalServer();
    }

    public static Identifier idOf(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
