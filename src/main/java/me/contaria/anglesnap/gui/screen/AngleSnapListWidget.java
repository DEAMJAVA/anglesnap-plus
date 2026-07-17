package me.contaria.anglesnap.gui.screen;

import me.contaria.anglesnap.AngleEntry;
import me.contaria.anglesnap.AngleSnap;
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
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AngleSnapListWidget extends ContainerObjectSelectionList<AngleSnapListWidget.AbstractEntry> {
    private static final Component NAME_TEXT = Component.translatable("anglesnap.gui.screen.name");
    private static final Component YAW_TEXT = Component.translatable("anglesnap.gui.screen.yaw");
    private static final Component PITCH_TEXT = Component.translatable("anglesnap.gui.screen.pitch");
    private static final Component ICON_TEXT = Component.translatable("anglesnap.gui.screen.icon");
    private static final Component COLOR_TEXT = Component.translatable("anglesnap.gui.screen.color");
    private static final Component ADD_TEXT = Component.translatable("anglesnap.gui.screen.add");
    private static final Component DELETE_TEXT = Component.translatable("anglesnap.gui.screen.delete");
    private static final Component EDIT_TEXT = Component.translatable("anglesnap.gui.screen.edit");
    private static final Component SAVE_TEXT = Component.translatable("anglesnap.gui.screen.save");

    private static final Identifier ADD_TEXTURE = AngleSnap.idOf("textures/gui/add.png");
    private static final Identifier DELETE_TEXTURE = AngleSnap.idOf("textures/gui/delete.png");
    private static final Identifier EDIT_TEXTURE = AngleSnap.idOf("textures/gui/edit.png");
    private static final Identifier SAVE_TEXTURE = AngleSnap.idOf("textures/gui/save.png");

    private static final int HOVERED_COLOR = ARGB.color(100, 200, 200, 200);

    private final AngleSnapScreen parent;

    public AngleSnapListWidget(Minecraft minecraftClient, int width, int height, int y, AngleSnapScreen parent) {
        super(minecraftClient, width, height, y, 20);
        this.parent = parent;

        this.addEntry(new Header(this.parent, NAME_TEXT, YAW_TEXT, PITCH_TEXT, ICON_TEXT, COLOR_TEXT));

        for (AngleEntry angle : AngleSnap.CONFIG.getAngles()) {
            this.addEntry(new me.contaria.anglesnap.gui.screen.AngleSnapListWidget.Entry(angle));
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
        private final StringWidget yawText;
        private final StringWidget pitchText;
        private final StringWidget iconText;
        private final StringWidget colorText;

        public Header(Screen parent, Component nameText, Component yawText, Component pitchText, Component iconText, Component colorText) {
            this.parent = parent;
            this.nameText = new StringWidget(nameText, parent.getFont());
            this.yawText = new StringWidget(yawText, parent.getFont());
            this.pitchText = new StringWidget(pitchText, parent.getFont());
            this.iconText = new StringWidget(iconText, parent.getFont());
            this.colorText = new StringWidget(colorText, parent.getFont());
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(nameText, yawText, pitchText, iconText, colorText);
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            this.nameText.setPosition(this.getContentX() + 5, this.getContentY() + (20 - this.parent.getFont().lineHeight) / 2);
            this.nameText.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

            this.yawText.setPosition(this.getContentX() + 5 + 5 * this.getContentWidth() / 17, this.getContentY() + (20 - this.parent.getFont().lineHeight) / 2);
            this.yawText.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

            this.pitchText.setPosition(this.getContentX() + 5 + 7 * this.getContentWidth() / 17, this.getContentY() + (20 - this.parent.getFont().lineHeight) / 2);
            this.pitchText.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

            this.iconText.setPosition(this.getContentX() + 5 + 9 * this.getContentWidth() / 17, this.getContentY() + (20 - this.parent.getFont().lineHeight) / 2);
            this.iconText.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);

            this.colorText.setPosition(this.getContentX() + 5 + 11 * this.getContentWidth() / 17, this.getContentY() + (20 - this.parent.getFont().lineHeight) / 2);
            this.colorText.extractWidgetRenderState(graphics, mouseX, mouseY, deltaTicks);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.nameText, this.yawText, this.pitchText, this.iconText, this.colorText);
        }
    }

    public class Entry extends AbstractEntry {
        private final AngleEntry angle;

        private final EditBox name;
        private final EditBox yaw;
        private final EditBox pitch;
        private final IconButtonWidget icon;
        private final EditBox color;
        private final IconButtonWidget edit;
        private final IconButtonWidget save;
        private final IconButtonWidget delete;

        private boolean editing;

        public Entry() {
            this(AngleSnap.CONFIG.createAngle());
            this.setEditing(true);
            this.setFocused(this.name);
        }

        public Entry(AngleEntry angle) {
            this.angle = angle;

            this.name = this.addChild(new EditBox(
                    this.client.font,
                    5 * AngleSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    NAME_TEXT
            ));
            this.name.setValue(this.angle.name);
            this.name.setResponder(name -> this.angle.name = name);
            this.name.setBordered(false);
            this.name.setTextColor(CommonColors.WHITE);
            this.name.setTextColorUneditable(CommonColors.WHITE);

            this.yaw = this.addChild(new EditBox(
                    this.client.font,
                    2 * AngleSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    YAW_TEXT
            ));
            this.yaw.setValue(String.valueOf(this.angle.yaw));
            this.yaw.setResponder(yaw -> {
                try {
                    this.angle.yaw = Float.parseFloat(yaw);
                } catch (NumberFormatException e) {
                    this.angle.yaw = 0.0f;
                }
            });
            this.yaw.setBordered(false);
            this.yaw.setTextColor(CommonColors.WHITE);
            this.yaw.setTextColorUneditable(CommonColors.WHITE);

            this.pitch = this.addChild(new EditBox(
                    this.client.font,
                    2 * AngleSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    PITCH_TEXT
            ));
            this.pitch.setValue(String.valueOf(this.angle.pitch));
            this.pitch.setResponder(pitch -> {
                try {
                    this.angle.pitch = Float.parseFloat(pitch);
                } catch (NumberFormatException e) {
                    this.angle.pitch = 0.0f;
                }
            });
            this.pitch.setBordered(false);
            this.pitch.setTextColor(CommonColors.WHITE);
            this.pitch.setTextColorUneditable(CommonColors.WHITE);

            this.icon = this.addChild(new IconButtonWidget(Component.empty(), button -> ((IconButtonWidget) button).setTexture(this.angle.nextIcon()), this.angle.getIcon()));

            this.color = this.addChild(new EditBox(
                    this.client.font,
                    3 * AngleSnapListWidget.this.getRowWidth() / 17 - 5,
                    20,
                    PITCH_TEXT
            ));
            this.color.setValue(colorToString(this.angle.color));
            this.color.setResponder(color -> {
                int maxLength = color.startsWith("#") ? 9 : 8;

                if (color.length() > maxLength) {
                    this.color.setValue(color.substring(0, maxLength));
                }

                this.angle.color = this.parseColor(color);
            });
            this.color.setBordered(false);
            this.color.setTextColor(CommonColors.WHITE);
            this.color.setTextColorUneditable(CommonColors.WHITE);

            this.edit = this.addChild(new IconButtonWidget(EDIT_TEXT, button -> this.toggleEditing(), EDIT_TEXTURE));
            this.save = this.addChild(new IconButtonWidget(SAVE_TEXT, button -> this.toggleEditing(), SAVE_TEXTURE));
            this.delete = this.addChild(new IconButtonWidget(DELETE_TEXT, button -> this.delete(), DELETE_TEXTURE));

            this.setEditing(false);
        }

        private String colorToString(int color) {
            return String.format("#%08X", Integer.rotateLeft(color, 8));
        }

        private int parseColor(String color) {
            if (color.startsWith("#")) {
                color = color.substring(1);
            }
            color = color.toLowerCase(Locale.ROOT);
            try {
                // pad RGB bits with 0's and alpha bit with f's
                // this means an RGB input as generated by some websites will also work
                String hex = StringUtils.rightPad(StringUtils.leftPad(color, 6, '0'), 8, 'f');
                return Integer.rotateRight(Integer.parseUnsignedInt(hex, 16), 8);
            } catch (NumberFormatException e) {
                return CommonColors.WHITE;
            }
        }

        private void toggleEditing() {
            this.setEditing(!this.editing);
        }

        private void setEditing(boolean editing) {
            this.editing = editing;

            this.setEditing(this.name, editing);
            this.setEditing(this.yaw, editing);
            this.setEditing(this.pitch, editing);
            this.setEditing(this.icon, editing);
            this.setEditing(this.color, editing);

            if (!editing) {
                this.yaw.setValue(String.valueOf(this.angle.yaw));
                this.pitch.setValue(String.valueOf(this.angle.pitch));
                this.color.setValue(this.colorToString(this.angle.color));
            }

            this.edit.visible = !editing;
            this.save.visible = editing;
        }

        private void setEditing(AbstractWidget widget, boolean editing) {
            widget.active = editing;
        }

        private void setEditing(EditBox widget, boolean editing) {
            widget.active = editing;
            widget.setEditable(editing);
            widget.setFocused(false);
            widget.setCanLoseFocus(editing);
            widget.moveCursorToStart(false);
        }

        private void delete() {
            AngleSnap.CONFIG.removeAngle(this.angle);
            AngleSnapListWidget.this.removeEntry(this);
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
            this.extractNumberWidgetAt(graphics, mouseX, mouseY, tickDelta, this.yaw, x + 5 + 5 * entryWidth / 17, textY);
            this.extractNumberWidgetAt(graphics, mouseX, mouseY, tickDelta, this.pitch, x + 5 + 7 * entryWidth / 17, textY);
            this.extractWidgetRenderStateAt(graphics, mouseX, mouseY, tickDelta, this.icon, x + 5 + 9 * entryWidth / 17, y);
            this.extractHexadecimalWidgetAt(graphics, mouseX, mouseY, tickDelta, this.color, x + 5 + 11 * entryWidth / 17, textY);
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

        private void extractNumberWidgetAt(GuiGraphicsExtractor graphicsExtractor, int mouseX, int mouseY, float tickDelta, EditBox widget, int x, int y) {
            if (this.isNumberOrEmpty(widget.getValue())) {
                this.extractTextWidgetAt(graphicsExtractor, mouseX, mouseY, tickDelta, widget, x, y);
            } else {
                widget.setTextColor(CommonColors.SOFT_RED);
                widget.setTextColorUneditable(CommonColors.SOFT_RED);
                this.extractTextWidgetAt(graphicsExtractor, mouseX, mouseY, tickDelta, widget, x, y);
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

        private void extractHexadecimalWidgetAt(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float tickDelta, EditBox widget, int x, int y) {
            if (this.isHexadecimalOrEmpty(widget.getValue())) {
                this.extractTextWidgetAt(guiGraphicsExtractor, mouseX, mouseY, tickDelta, widget, x, y);
            } else {
                widget.setTextColor(CommonColors.SOFT_RED);
                widget.setTextColorUneditable(CommonColors.SOFT_RED);
                this.extractTextWidgetAt(guiGraphicsExtractor, mouseX, mouseY, tickDelta, widget, x, y);
                widget.setTextColor(CommonColors.WHITE);
                widget.setTextColorUneditable(CommonColors.WHITE);
            }
        }

        private boolean isHexadecimalOrEmpty(String text) {
            if (text.startsWith("#")) {
                text = text.substring(1);
            }
            if (text.isEmpty()) {
                return true;
            }
            text = text.toLowerCase(Locale.ROOT);
            try {
                // noinspection ResultOfMethodCallIgnored
                Integer.parseUnsignedInt(text, 16);
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
            if (!this.editing && click.button() == 0) {
                AngleSnapListWidget.this.parent.snap(this.angle);
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
            me.contaria.anglesnap.gui.screen.AngleSnapListWidget.Entry entry = new me.contaria.anglesnap.gui.screen.AngleSnapListWidget.Entry();
            AngleSnapListWidget.this.removeEntry(this);
            AngleSnapListWidget.this.addEntry(entry);
            AngleSnapListWidget.this.addEntry(this);
            AngleSnapListWidget.this.setFocused(entry);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
            super.mouseClicked(click, doubled);
            return false;
        }
    }
}
