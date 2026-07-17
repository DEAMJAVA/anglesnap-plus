package me.contaria.anglesnap.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Matrix4fc;

import java.util.List;

public class IconFeatureRenderer extends RenderTypeFeatureRenderer<IconFeatureRenderer.Submit> {
    public static final FeatureRendererType<Submit> TYPE = FeatureRendererType.create("icon_submit");

    @Override
    protected void buildGroup(FeatureFrameContext context, List<Submit> submits) {
        for (Submit submit : submits) {
            VertexConsumer consumer = this.getVertexBuilder(submit.renderType());
            Matrix4fc pose = submit.pose();

            consumer.addVertex(pose, -1.0f, -1.0f, 0.0f).setColor(submit.color()).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0, 0, 1);
            consumer.addVertex(pose, -1.0f, 1.0f, 0.0f).setColor(submit.color()).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0, 0, 1);
            consumer.addVertex(pose, 1.0f, 1.0f, 0.0f).setColor(submit.color()).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0, 0, 1);
            consumer.addVertex(pose, 1.0f, -1.0f, 0.0f).setColor(submit.color()).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(0, 0, 1);
        }
    }

    public record Submit(Matrix4fc pose, RenderType renderType, int color) implements SubmitNode {
        @Override
        public FeatureRendererType<Submit> featureType() {
            return IconFeatureRenderer.TYPE;
        }
    }
}