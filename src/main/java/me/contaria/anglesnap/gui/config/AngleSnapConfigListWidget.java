package me.contaria.anglesnap.gui.config;

import me.contaria.anglesnap.AngleSnap;
import me.contaria.anglesnap.config.Option;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import java.util.ArrayList;
import java.util.List;

public class AngleSnapConfigListWidget extends ContainerObjectSelectionList<AngleSnapConfigListWidget.AbstractEntry> {
    private static final int HOVERED_COLOR = ARGB.color(100, 200, 200, 200);

    public AngleSnapConfigListWidget(Minecraft minecraftClient, int width, int height, int y) {
        super(minecraftClient, width, height, y, 24);

        for (Option<?> option : AngleSnap.CONFIG.getOptions()) {
            if (option.hasWidget()) {
                this.addEntry(new me.contaria.anglesnap.gui.config.AngleSnapConfigListWidget.Entry(option));
            }
        }
    }

    @Override
    public int getRowLeft() {
        return 6;
    }

    @Override
    public int getRowWidth() {
        return this.width - 12;
    }

    @Override
    protected int scrollBarX() {
        return this.width - 6;
    }

    public abstract static class AbstractEntry extends ContainerObjectSelectionList.Entry<AbstractEntry> {
        protected final Minecraft client;

        protected AbstractEntry(Minecraft client) {
            this.client = client;
        }

        protected void extractWidgetRenderStateAt(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float tickDelta, AbstractWidget widget, int x, int y) {
            widget.setX(x);
            widget.setY(y);
            widget.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, tickDelta);
        }
    }

    public static class Entry extends AbstractEntry {
        private final Option<?> option;

        private final List<AbstractWidget> children;
        private final AbstractWidget widget;

        public Entry(Option<?> option) {
            super(Minecraft.getInstance());
            this.option = option;
            this.children = new ArrayList<>();
            this.widget = this.addChild(option.createWidget(0, 0, 150, 20));
        }

        private <T extends AbstractWidget> T addChild(T widget) {
            this.children.add(widget);
            return widget;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (!focused) {
                this.setFocused(null);
            }
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.getContentX();
            int y = this.getContentY();
            int entryWidth = this.getContentWidth();
            int entryHeight = this.getContentHeight();

            Font textRenderer = this.client.font;
            if (hovered) {
                graphics.fill(x, y, x + entryWidth, y + entryHeight, HOVERED_COLOR);
            }
            int textY = y + (entryHeight - textRenderer.lineHeight + 1) / 2;
            graphics.text(textRenderer, this.option.getName(), x + 5, textY, CommonColors.WHITE, true);
            this.extractWidgetRenderStateAt(graphics, mouseX, mouseY, tickDelta, this.widget, x + 155, y);
        }
    }
}
