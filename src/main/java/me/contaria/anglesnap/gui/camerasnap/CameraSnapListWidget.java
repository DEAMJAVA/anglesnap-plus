package me.contaria.anglesnap.gui.camerasnap;

import me.contaria.anglesnap.AngleSnap;
import me.contaria.anglesnap.CameraPosEntry;
import me.contaria.anglesnap.gui.screen.IconButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;

public class CameraSnapListWidget extends ElementListWidget<CameraSnapListWidget.AbstractEntry> {
    private static final Text NAME_TEXT = Text.translatable("anglesnap.gui.screen.name");
    private static final Text X_TEXT = Text.translatable("anglesnap.gui.screen.x");
    private static final Text Y_TEXT = Text.translatable("anglesnap.gui.screen.y");
    private static final Text Z_TEXT = Text.translatable("anglesnap.gui.screen.z");
    private static final Text ADD_TEXT = Text.translatable("anglesnap.gui.screen.add");
    private static final Text DELETE_TEXT = Text.translatable("anglesnap.gui.screen.delete");
    private static final Text EDIT_TEXT = Text.translatable("anglesnap.gui.screen.edit");
    private static final Text SAVE_TEXT = Text.translatable("anglesnap.gui.screen.save");

    private static final Identifier ADD_TEXTURE = Identifier.of("anglesnap", "textures/gui/add.png");
    private static final Identifier DELETE_TEXTURE = Identifier.of("anglesnap", "textures/gui/delete.png");
    private static final Identifier EDIT_TEXTURE = Identifier.of("anglesnap", "textures/gui/edit.png");
    private static final Identifier SAVE_TEXTURE = Identifier.of("anglesnap", "textures/gui/save.png");

    private static final int HOVERED_COLOR = ColorHelper.getArgb(100, 200, 200, 200);

    private final CameraSnapScreen cameraSnapScreen;

    public CameraSnapListWidget(MinecraftClient minecraftClient, CameraSnapScreen cameraSnapScreen, int width, int height, int y) {
        super(minecraftClient, width, height, y, 20);

        this.cameraSnapScreen = cameraSnapScreen;

        this.addEntry(new Header(this.cameraSnapScreen, NAME_TEXT, X_TEXT, Y_TEXT, Z_TEXT));
        for (CameraPosEntry pos : AngleSnap.CONFIG.getCameraPositions()) {
            this.addEntry(new Entry(pos));
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
    protected int getScrollbarX() {
        return this.width - 6;
    }

    public abstract static class AbstractEntry extends ElementListWidget.Entry<AbstractEntry> {
        protected final MinecraftClient client;
        protected final List<ClickableWidget> children;

        protected AbstractEntry() {
            this.client = MinecraftClient.getInstance();
            this.children = new ArrayList<>();
        }

        protected <T extends ClickableWidget> T addChild(T widget) {
            this.children.add(widget);
            return widget;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return this.children;
        }

        @Override
        public List<? extends Element> children() {
            return this.children;
        }

        @Override
        public void setFocused(boolean focused) {
            super.setFocused(focused);
            if (!focused) {
                this.setFocused(null);
            }
        }

        protected void renderWidgetAt(DrawContext context, int mouseX, int mouseY, float tickDelta, ClickableWidget widget, int x, int y) {
            widget.setX(x);
            widget.setY(y);
            widget.render(context, mouseX, mouseY, tickDelta);
        }
    }

    public class Header extends AbstractEntry {
        private final Screen parent;
        private final TextWidget nameText;
        private final TextWidget xText;
        private final TextWidget yText;
        private final TextWidget zText;

        public Header(Screen parent, Text nameText, Text xText, Text yText, Text zText) {
            this.parent = parent;
            this.nameText = new TextWidget(nameText, this.parent.getTextRenderer());
            this.xText = new TextWidget(xText, this.parent.getTextRenderer());
            this.yText = new TextWidget(yText, this.parent.getTextRenderer());
            this.zText = new TextWidget(zText, this.parent.getTextRenderer());
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of(nameText, xText, yText, zText);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            this.nameText.setPosition(this.getContentX() + 5, this.getContentY() + (20 - this.parent.getTextRenderer().fontHeight) / 2);
            this.nameText.render(context, mouseX, mouseY, deltaTicks);
            this.xText.setPosition(this.getContentX() + 5 + 5 * this.getContentWidth() / 17, this.getContentY() + (20 - this.parent.getTextRenderer().fontHeight) / 2);
            this.xText.render(context, mouseX, mouseY, deltaTicks);
            this.yText.setPosition(this.getContentX() + 5 + 7 * this.getContentWidth() / 17, this.getContentY() + (20 - this.parent.getTextRenderer().fontHeight) / 2);
            this.yText.render(context, mouseX, mouseY, deltaTicks);
            this.zText.setPosition(this.getContentX() + 5 + 9 * this.getContentWidth() / 17, this.getContentY() + (20 - this.parent.getTextRenderer().fontHeight) / 2);
            this.zText.render(context, mouseX, mouseY, deltaTicks);
        }
    }

    public class Entry extends AbstractEntry {
        private final CameraPosEntry pos;

        private final TextFieldWidget name;
        private final TextFieldWidget x;
        private final TextFieldWidget y;
        private final TextFieldWidget z;
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

            this.name = this.addChild(new TextFieldWidget(
                    this.client.textRenderer,
                    5 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    NAME_TEXT
            ));
            this.name.setText(this.pos.name);
            this.name.setChangedListener(name -> this.pos.name = name);
            this.name.setDrawsBackground(false);
            this.name.setEditableColor(Colors.WHITE);
            this.name.setUneditableColor(Colors.WHITE);

            this.x = this.addChild(new TextFieldWidget(
                    this.client.textRenderer,
                    2 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    X_TEXT
            ));
            this.x.setText(String.valueOf(this.pos.x));
            this.x.setChangedListener(yaw -> {
                try {
                    this.pos.x = Double.parseDouble(yaw);
                } catch (NumberFormatException e) {
                    this.pos.x = 0.0;
                }
            });
            this.x.setDrawsBackground(false);
            this.x.setEditableColor(Colors.WHITE);
            this.x.setUneditableColor(Colors.WHITE);

            this.y = this.addChild(new TextFieldWidget(
                    this.client.textRenderer,
                    2 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    Y_TEXT
            ));
            this.y.setText(String.valueOf(this.pos.y));
            this.y.setChangedListener(yaw -> {
                try {
                    this.pos.y = Double.parseDouble(yaw);
                } catch (NumberFormatException e) {
                    this.pos.y = 0.0;
                }
            });
            this.y.setDrawsBackground(false);
            this.y.setEditableColor(Colors.WHITE);
            this.y.setUneditableColor(Colors.WHITE);

            this.z = this.addChild(new TextFieldWidget(
                    this.client.textRenderer,
                    2 * CameraSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    Z_TEXT
            ));
            this.z.setText(String.valueOf(this.pos.z));
            this.z.setChangedListener(yaw -> {
                try {
                    this.pos.z = Double.parseDouble(yaw);
                } catch (NumberFormatException e) {
                    this.pos.z = 0.0;
                }
            });
            this.z.setDrawsBackground(false);
            this.z.setEditableColor(Colors.WHITE);
            this.z.setUneditableColor(Colors.WHITE);

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
                this.x.setText(String.valueOf(this.pos.x));
                this.y.setText(String.valueOf(this.pos.y));
                this.z.setText(String.valueOf(this.pos.z));
            }

            this.edit.visible = !editing;
            this.save.visible = editing;
        }

        private void setEditing(TextFieldWidget widget, boolean editing) {
            widget.active = editing;
            widget.setEditable(editing);
            widget.setFocused(false);
            widget.setFocusUnlocked(editing);
            widget.setCursorToStart(false);
        }

        private void delete() {
            AngleSnap.CONFIG.removeCameraPosition(this.pos);
            CameraSnapListWidget.this.removeEntry(this);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.getContentX();
            int y = this.getContentY();
            int entryWidth = this.getContentWidth();
            int entryHeight = this.getContentHeight();

            if (hovered) {
                context.fill(x, y, x + entryWidth, y + entryHeight, HOVERED_COLOR);
            }
            int textY = y + (entryHeight - this.client.textRenderer.fontHeight + 1) / 2;
            this.renderTextWidgetAt(context, mouseX, mouseY, tickDelta, this.name, x + 5, textY);
            this.renderNumberWidgetAt(context, mouseX, mouseY, tickDelta, this.x, x + 5 + 5 * entryWidth / 17, textY);
            this.renderNumberWidgetAt(context, mouseX, mouseY, tickDelta, this.y, x + 5 + 7 * entryWidth / 17, textY);
            this.renderNumberWidgetAt(context, mouseX, mouseY, tickDelta, this.z, x + 5 + 9 * entryWidth / 17, textY);
            this.renderWidgetAt(context, mouseX, mouseY, tickDelta, this.edit, x + entryWidth - 5 - 40, y);
            this.renderWidgetAt(context, mouseX, mouseY, tickDelta, this.save, x + entryWidth - 5 - 40, y);
            this.renderWidgetAt(context, mouseX, mouseY, tickDelta, this.delete, x + entryWidth - 5 - 20, y);
        }

        private void renderTextWidgetAt(DrawContext context, int mouseX, int mouseY, float tickDelta, TextFieldWidget widget, int x, int y) {
            if (this.editing) {
                this.renderWidgetAt(context, mouseX, mouseY, tickDelta, widget, x, y);
            } else {
                context.drawText(this.client.textRenderer, widget.getText(), x, y, Colors.WHITE, true);
            }
        }

        private void renderNumberWidgetAt(DrawContext context, int mouseX, int mouseY, float tickDelta, TextFieldWidget widget, int x, int y) {
            if (this.isNumberOrEmpty(widget.getText())) {
                this.renderTextWidgetAt(context, mouseX, mouseY, tickDelta, widget, x, y);
            } else {
                widget.setEditableColor(Colors.LIGHT_RED);
                widget.setUneditableColor(Colors.LIGHT_RED);
                this.renderTextWidgetAt(context, mouseX, mouseY, tickDelta, widget, x, y);
                widget.setEditableColor(Colors.WHITE);
                widget.setUneditableColor(Colors.WHITE);
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
        public boolean mouseClicked(Click click, boolean doubled) {
            if (super.mouseClicked(click, doubled)) {
                return true;
            }
            if (!this.editing && click.button() == 0) {
                AngleSnap.currentCameraPos = this.pos;
                return true;
            }
            return false;
        }
    }

    public class AddAngleEntry extends AbstractEntry {
        private final ButtonWidget add;

        public AddAngleEntry() {
            this.add = this.addChild(new IconButtonWidget(ADD_TEXT, button -> this.add(), ADD_TEXTURE));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.renderWidgetAt(context, mouseX, mouseY, tickDelta, this.add, this.getContentX() + 5, this.getContentY() + (this.getContentHeight() - this.client.textRenderer.fontHeight) / 2);
        }

        private void add() {
            Entry entry = new Entry();
            CameraSnapListWidget.this.removeEntry(this);
            CameraSnapListWidget.this.addEntry(entry);
            CameraSnapListWidget.this.addEntry(this);
            CameraSnapListWidget.this.setFocused(entry);
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            super.mouseClicked(click, doubled);
            return false;
        }
    }
}
