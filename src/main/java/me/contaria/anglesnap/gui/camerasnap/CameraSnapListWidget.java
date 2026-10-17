package me.contaria.anglesnap.gui.camerasnap;

import com.mojang.blaze3d.platform.InputConstants;
import me.contaria.anglesnap.AngleSnap;
import me.contaria.anglesnap.CameraPosEntry;
import me.contaria.anglesnap.gui.screen.IconButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import java.util.ArrayList;
import java.util.List;

public class CameraSnapListWidget extends ContainerObjectSelectionList<CameraSnapListWidget.AbstractEntry> {
    private static final Component NAME_TEXT = Component.translatable("anglesnap.gui.screen.name");
    private static final Component X_TEXT = Component.translatable("anglesnap.gui.screen.x");
    private static final Component Y_TEXT = Component.translatable("anglesnap.gui.screen.y");
    private static final Component Z_TEXT = Component.translatable("anglesnap.gui.screen.z");
    private static final Component ADD_TEXT = Component.translatable("anglesnap.gui.screen.add");
    private static final Component DELETE_TEXT = Component.translatable("anglesnap.gui.screen.delete");
    private static final Component EDIT_TEXT = Component.translatable("anglesnap.gui.screen.edit");
    private static final Component SAVE_TEXT = Component.translatable("anglesnap.gui.screen.save");

    private static final Identifier ADD_TEXTURE = AngleSnap.idOf("textures/gui/add.png");
    private static final Identifier DELETE_TEXTURE = AngleSnap.idOf("textures/gui/delete.png");
    private static final Identifier EDIT_TEXTURE = AngleSnap.idOf("textures/gui/edit.png");
    private static final Identifier SAVE_TEXTURE = AngleSnap.idOf("textures/gui/save.png");

    private static final int HOVERED_COLOR = ARGB.color(100, 200, 200, 200);

    private final CameraSnapScreen cameraSnapScreen;

    public CameraSnapListWidget(Minecraft minecraftClient, CameraSnapScreen cameraSnapScreen, int width, int height, int y) {
        super(minecraftClient, width, height, y, 20);

        this.cameraSnapScreen = cameraSnapScreen;

        this.addEntry(new Header(this.cameraSnapScreen, NAME_TEXT, X_TEXT, Y_TEXT, Z_TEXT));
        for (CameraPosEntry pos : AngleSnap.CONFIG.getCameraPositions()) {
            this.addEntry(new me.contaria.anglesnap.gui.camerasnap.CameraSnapListWidget.Entry(pos));
        }
        this.addEntry(new AddAngleEntry());
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
        protected final List<AbstractWidget> children;

        protected AbstractEntry() {
            this.client = Minecraft.getInstance();
            this.children = new ArrayList<>();
        }

        protected <T extends AbstractWidget> T addChild(T widget) {
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

        protected void extractWidgetRenderStateAt(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float tickDelta, AbstractWidget widget, int x, int y) {
            widget.setX(x);
            widget.setY(y);
            widget.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, tickDelta);
        }
    }

    public class Header extends AbstractEntry {
        private final Screen parent;
        private final StringWidget nameText;
        private final StringWidget xText;
        private final StringWidget yText;
        private final StringWidget zText;

        public Header(Screen parent, Component nameText, Component xText, Component yText, Component zText) {
            this.parent = parent;
            this.nameText = new StringWidget(nameText, this.parent.getFont());
            this.xText = new StringWidget(xText, this.parent.getFont());
            this.yText = new StringWidget(yText, this.parent.getFont());
            this.zText = new StringWidget(zText, this.parent.getFont());
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(nameText, xText, yText, zText);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            this.nameText.setPosition(this.getContentX() + 5, this.getContentY() + (20 - this.parent.getFont().lineHeight) / 2);
            this.nameText.extractWidgetRenderState(graphicsExtractor, mouseX, mouseY, deltaTicks);

            this.xText.setPosition(this.getContentX() + 5 + 5 * this.getContentWidth() / 17, this.getContentY() + (20 - this.parent.getFont().lineHeight) / 2);
            this.xText.extractWidgetRenderState(graphicsExtractor, mouseX, mouseY, deltaTicks);

            this.yText.setPosition(this.getContentX() + 5 + 7 * this.getContentWidth() / 17, this.getContentY() + (20 - this.parent.getFont().lineHeight) / 2);
            this.yText.extractWidgetRenderState(graphicsExtractor, mouseX, mouseY, deltaTicks);

            this.zText.setPosition(this.getContentX() + 5 + 9 * this.getContentWidth() / 17, this.getContentY() + (20 - this.parent.getFont().lineHeight) / 2);
            this.zText.extractWidgetRenderState(graphicsExtractor, mouseX, mouseY, deltaTicks);
        }
    }

    public class Entry extends AbstractEntry {
        private final CameraPosEntry pos;

        private final EditBox name;
        private final EditBox x;
        private final EditBox y;
        private final EditBox z;
        private final IconButtonWidget edit;
        private final IconButtonWidget save;
        private final IconButtonWidget delete;

        private boolean editing;

        public Entry() {
            this(AngleSnap.CONFIG.createCameraPosition());
            this.setEditing(true);
            this.setFocused(this.name);
        }

        public Entry(CameraPosEntry pos) {
            this.pos = pos;

            this.name = this.addChild(new EditBox(
                    this.client.font,
                    5 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    NAME_TEXT
            ));
            this.name.setValue(this.pos.name);
            this.name.setResponder(name -> this.pos.name = name);
            this.name.setBordered(false);
            this.name.setTextColor(CommonColors.WHITE);
            this.name.setTextColorUneditable(CommonColors.WHITE);

            this.x = this.addChild(new EditBox(
                    this.client.font,
                    2 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    X_TEXT
            ));
            this.x.setValue(String.valueOf(this.pos.x));
            this.x.setResponder(yaw -> {
                try {
                    this.pos.x = Double.parseDouble(yaw);
                } catch (NumberFormatException e) {
                    this.pos.x = 0.0;
                }
            });
            this.x.setBordered(false);
            this.x.setTextColor(CommonColors.WHITE);
            this.x.setTextColorUneditable(CommonColors.WHITE);

            this.y = this.addChild(new EditBox(
                    this.client.font,
                    2 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    Y_TEXT
            ));
            this.y.setValue(String.valueOf(this.pos.y));
            this.y.setResponder(yaw -> {
                try {
                    this.pos.y = Double.parseDouble(yaw);
                } catch (NumberFormatException e) {
                    this.pos.y = 0.0;
                }
            });
            this.y.setBordered(false);
            this.y.setTextColor(CommonColors.WHITE);
            this.y.setTextColorUneditable(CommonColors.WHITE);

            this.z = this.addChild(new EditBox(
                    this.client.font,
                    2 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    Z_TEXT
            ));
            this.z.setValue(String.valueOf(this.pos.z));
            this.z.setResponder(yaw -> {
                try {
                    this.pos.z = Double.parseDouble(yaw);
                } catch (NumberFormatException e) {
                    this.pos.z = 0.0;
                }
            });
            this.z.setBordered(false);
            this.z.setTextColor(CommonColors.WHITE);
            this.z.setTextColorUneditable(CommonColors.WHITE);

            this.edit = this.addChild(new IconButtonWidget(EDIT_TEXT, button -> this.toggleEditing(), EDIT_TEXTURE));
            this.save = this.addChild(new IconButtonWidget(SAVE_TEXT, button -> this.toggleEditing(), SAVE_TEXTURE));
            this.delete = this.addChild(new IconButtonWidget(DELETE_TEXT, button -> this.delete(), DELETE_TEXTURE));

            this.setEditing(false);
        }

        private void toggleEditing() {
            this.setEditing(!this.editing);
        }

        private void setEditing(boolean editing) {
            this.editing = editing;

            this.setEditing(this.name, editing);
            this.setEditing(this.x, editing);
            this.setEditing(this.y, editing);
            this.setEditing(this.z, editing);

            if (!editing) {
                this.x.setValue(String.valueOf(this.pos.x));
                this.y.setValue(String.valueOf(this.pos.y));
                this.z.setValue(String.valueOf(this.pos.z));
            }

            this.edit.visible = !editing;
            this.save.visible = editing;
        }

        private void setEditing(EditBox widget, boolean editing) {
            widget.active = editing;
            widget.setEditable(editing);
            widget.setFocused(false);
            widget.setCanLoseFocus(editing);
            widget.moveCursorToStart(false);
        }

        private void delete() {
            AngleSnap.CONFIG.removeCameraPosition(this.pos);
            CameraSnapListWidget.this.removeEntry(this);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.getContentX();
            int y = this.getContentY();
            int entryWidth = this.getContentWidth();
            int entryHeight = this.getContentHeight();

            if (hovered) {
                graphics.fill(x, y, x + entryWidth, y + entryHeight, HOVERED_COLOR);
            }
            int textY = y + (entryHeight - this.client.font.lineHeight + 1) / 2;
            this.extractTextWidgetAt(graphics, mouseX, mouseY, tickDelta, this.name, x + 5, textY);
            this.extractNumberWidgetAt(graphics, mouseX, mouseY, tickDelta, this.x, x + 5 + 5 * entryWidth / 17, textY);
            this.extractNumberWidgetAt(graphics, mouseX, mouseY, tickDelta, this.y, x + 5 + 7 * entryWidth / 17, textY);
            this.extractNumberWidgetAt(graphics, mouseX, mouseY, tickDelta, this.z, x + 5 + 9 * entryWidth / 17, textY);
            this.extractWidgetRenderStateAt(graphics, mouseX, mouseY, tickDelta, this.edit, x + entryWidth - 5 - 40, y);
            this.extractWidgetRenderStateAt(graphics, mouseX, mouseY, tickDelta, this.save, x + entryWidth - 5 - 40, y);
            this.extractWidgetRenderStateAt(graphics, mouseX, mouseY, tickDelta, this.delete, x + entryWidth - 5 - 20, y);
        }

        private void extractTextWidgetAt(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float tickDelta, EditBox widget, int x, int y) {
            if (this.editing) {
                this.extractWidgetRenderStateAt(guiGraphicsExtractor, mouseX, mouseY, tickDelta, widget, x, y);
            } else {
                guiGraphicsExtractor.text(this.client.font, widget.getValue(), x, y, CommonColors.WHITE, true);
            }
        }

        private void extractNumberWidgetAt(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float tickDelta, EditBox widget, int x, int y) {
            if (this.isNumberOrEmpty(widget.getValue())) {
                this.extractTextWidgetAt(guiGraphicsExtractor, mouseX, mouseY, tickDelta, widget, x, y);
            } else {
                widget.setTextColor(CommonColors.SOFT_RED);
                widget.setTextColorUneditable(CommonColors.SOFT_RED);
                this.extractTextWidgetAt(guiGraphicsExtractor, mouseX, mouseY, tickDelta, widget, x, y);
                widget.setTextColor(CommonColors.WHITE);
                widget.setTextColorUneditable(CommonColors.WHITE);
            }
        }

        private boolean isNumberOrEmpty(String text) {
            if (text.isEmpty()) {
                return true;
            }
            try {
                Float.parseFloat(text);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            if (super.mouseClicked(click, doubled)) {
                return true;
            }
            if (!this.editing && click.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                AngleSnap.currentCameraPos = this.pos;
                return true;
            }
            return false;
        }
    }

    public class AddAngleEntry extends AbstractEntry {
        private final Button add;

        public AddAngleEntry() {
            this.add = this.addChild(new IconButtonWidget(ADD_TEXT, button -> this.add(), ADD_TEXTURE));
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.extractWidgetRenderStateAt(graphics, mouseX, mouseY, tickDelta, this.add, this.getContentX() + 5, this.getContentY() + (this.getContentHeight() - this.client.font.lineHeight) / 2);
        }

        private void add() {
            me.contaria.anglesnap.gui.camerasnap.CameraSnapListWidget.Entry entry = new me.contaria.anglesnap.gui.camerasnap.CameraSnapListWidget.Entry();
            CameraSnapListWidget.this.removeEntry(this);
            CameraSnapListWidget.this.addEntry(entry);
            CameraSnapListWidget.this.addEntry(this);
            CameraSnapListWidget.this.setFocused(entry);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            super.mouseClicked(click, doubled);
            return false;
        }
    }
}
