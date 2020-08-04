package ovh.corail.tombstone.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ovh.corail.tombstone.api.capability.ITBCapability;
import ovh.corail.tombstone.api.capability.Perk;
import ovh.corail.tombstone.helper.Helper;
import ovh.corail.tombstone.helper.LangKey;
import ovh.corail.tombstone.network.PacketHandler;
import ovh.corail.tombstone.network.UpgradePerkServerMessage;
import ovh.corail.tombstone.perk.PerkRegistry;
import ovh.corail.tombstone.registry.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@OnlyIn(Dist.CLIENT)
public class GuiKnowledge extends TBScreen {
    private static final ResourceLocation GUI_BAR = new ResourceLocation("minecraft", "textures/gui/bars.png");
    private final ITBCapability cap;
    private final int iconSize = 16;
    private final List<PerkIcon> icons = new ArrayList<>();
    private PerkIcon hoveredIcon;
    private int leftPerkPoints;
    private int hoveredPerkLevel = 0;
    private final ItemStack stackSkull = new ItemStack(ModItems.advancement[0]), stackRevive = new ItemStack(ModItems.advancement[7]), stackAnkh = new ItemStack(ModItems.advancement[3]);
    private final double alignmentPos;

    public GuiKnowledge(ITBCapability cap) {
        super(LangKey.MESSAGE_KNOWLEDGE_OF_DEATH.getText());
        this.cap = cap;
        for (Perk perk : PerkRegistry.perkRegistry.getValues()) {
            if (!Helper.isDisabledPerk(perk, Minecraft.getInstance().player)) {
                this.icons.add(new PerkIcon(perk));
            }
        }
        this.alignmentPos = getBarRatio();
    }

    @Override
    public void init() {
        super.init();
        int x = this.guiLeft + 15;
        int y = this.guiTop + 72;
        int i = 0;
        int spaceForIcon = this.icons.size() > 12 ? 22 : 31;
        int maxIconByLine = this.icons.size() > 12 ? 8 : 6;
        for (PerkIcon icon : this.icons) {
            int caseX = i % maxIconByLine;
            icon.setPosition(x + caseX * spaceForIcon, y);
            if (caseX == (maxIconByLine - 1)) {
                y += 30;
            }
            i++;
        }
        addButton(new TBGuiButton(this.guiLeft + 10, this.guiBottom - 25, 70, 15, new TranslationTextComponent("tombstone.compendium.main.title"), pressable -> getMinecraft().displayGuiScreen(new GuiInfo(null))));
        addButton(new TBGuiButton(this.guiRight - 10 - 70, this.guiBottom - 25, 70, 15, LangKey.BUTTON_CONFIG.getText(), pressable -> getMinecraft().displayGuiScreen(new GuiConfig())));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (this.hoveredIcon != null) {
            if (mouseButton == 0) {
                int perkLevel = this.cap.getPerkLevel(getMinecraft().player, this.hoveredIcon.perk);
                if (perkLevel < this.hoveredIcon.perk.getLevelMax() && this.leftPerkPoints >= this.hoveredIcon.perk.getCost(perkLevel + 1)) {
                    PacketHandler.sendToServer(new UpgradePerkServerMessage(UpgradePerkServerMessage.SyncType.UPGRADE_PERK, this.hoveredIcon.perk));
                    return true;
                }
            } else if (mouseButton == 1 && getMinecraft().player.isCreative()) {
                if (cap.getPerkLevel(getMinecraft().player, this.hoveredIcon.perk) > 0) {
                    PacketHandler.sendToServer(new UpgradePerkServerMessage(UpgradePerkServerMessage.SyncType.DOWNGRADE_PERK, this.hoveredIcon.perk));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(matrixStack);

        this.hoveredIcon = this.icons.stream().filter(p -> p.contains(mouseX, mouseY)).findFirst().orElse(null);
        int totalPerkPoints = this.cap.getTotalPerkPoints();
        int usedPerkPoints = this.cap.getUsedPerkPoints(getMinecraft().player);
        this.leftPerkPoints = totalPerkPoints - usedPerkPoints;

        RenderSystem.pushMatrix();
        RenderSystem.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        RenderSystem.disableLighting();
        RenderSystem.disableDepthTest();
        RenderSystem.color4f(1f, 1f, 1f, 1f);

        hLine(matrixStack, this.guiLeft + 5, this.guiRight - 5, this.guiTop + 4, this.textColor);
        hLine(matrixStack, this.guiLeft + 5, this.guiRight - 5, this.guiTop + 6, this.textColor);
        String titleName = LangKey.MESSAGE_KNOWLEDGE_OF_DEATH.getClientTranslation();
        this.font.drawString(matrixStack, titleName, this.halfWidth - this.font.getStringWidth(titleName) / 2f, this.guiTop + 9f, this.textColor);
        hLine(matrixStack, this.guiLeft + 5, this.guiRight - 5, this.guiTop + 18, this.textColor);
        hLine(matrixStack, this.guiLeft + 5, this.guiRight - 5, this.guiTop + 20, this.textColor);

        RenderSystem.color4f(1f, 1f, 1f, 1f);
        getMinecraft().textureManager.bindTexture(GUI_BAR);
        blit(matrixStack, this.guiLeft + 52, this.guiTop + 30, 0, 40, 182, 5, 182, 256);
        blit(matrixStack, this.guiLeft + 52, this.guiTop + 30, 0, 45, (int) (182 * 0.71d * (this.cap.getKnowledge() - this.cap.getKnowledgeForLevel(totalPerkPoints)) / this.cap.getKnowledgeToReachNextLevel(totalPerkPoints + 1)), 5, 182, 256);
        blit(matrixStack, this.guiLeft + 52, this.guiTop + 31, 0, 101, 182, 3, 182, 256);

        int startAlignmentY = 44;
        drawCenteredString(matrixStack, this.font, "Alignment", this.halfWidth, this.guiTop + startAlignmentY - 4, 0xffffffff);
        fill(matrixStack, this.guiLeft + 20, this.guiTop + startAlignmentY + 10, this.guiRight - 20, this.guiTop + startAlignmentY + 17, 0xff000000);
        Helper.fillGradient(this.guiLeft + 21, this.guiTop + startAlignmentY + 11, this.halfWidth, this.guiTop + startAlignmentY + 16, 0xffff0000, 0xffffffff, getBlitOffset(), true);
        Helper.fillGradient(this.halfWidth, this.guiTop + startAlignmentY + 11, this.guiRight - 21, this.guiTop + startAlignmentY + 16, 0xffffffff, 0xff0000ff, getBlitOffset(), true);
        float step = (this.xSize - 42) / 8f;
        for (int i = 0; i < 8; i++) {
            if (i != 4) {
                vLine(matrixStack, this.guiLeft + 21 + (int) (i * step), this.guiTop + startAlignmentY + 10, this.guiTop + startAlignmentY + 16, 0xff000000);
            }
        }
        this.itemRenderer.renderItemAndEffectIntoGUI(this.stackSkull, this.guiLeft + 10, this.guiTop + startAlignmentY + 5);
        this.itemRenderer.renderItemAndEffectIntoGUI(this.stackRevive, this.guiRight - 10 - 16, this.guiTop + startAlignmentY + 5);
        int adjust = (int) Math.round((this.xSize - 42) / 2d * this.alignmentPos);
        this.itemRenderer.renderItemAndEffectIntoGUI(this.stackAnkh, this.halfWidth + adjust - 8, this.guiTop + startAlignmentY + 4);

        String levelString = this.leftPerkPoints + " / " + totalPerkPoints;
        this.font.drawString(matrixStack, levelString, this.guiLeft + 48 - this.font.getStringWidth(levelString), this.guiTop + 28, this.textColor);
        for (PerkIcon icon : this.icons) {
            drawPerk(matrixStack, icon);
        }
        if (Helper.isContributor(getMinecraft().player)) {
            this.font.drawString(matrixStack, "Contributor", this.guiLeft + 30, this.guiBottom - 45, this.bonusColor);
        }
        if (Helper.isDateAroundHalloween()) {
            String halloweenString = "Halloween";
            this.font.drawString(matrixStack, halloweenString, this.guiRight - 30 - this.font.getStringWidth(halloweenString), this.guiBottom - 45, this.bonusColor);
        }
        super.render(matrixStack, mouseX, mouseY, partialTick);
        drawPerkTooltip(matrixStack);

        RenderSystem.enableLighting();
        RenderSystem.enableDepthTest();
        //PortingHelper.enableStandardItemLighting();
        RenderSystem.enableRescaleNormal();
        RenderSystem.popMatrix();
    }

    private double getBarRatio() {
        final boolean isPositive = this.cap.getAlignmentValue() >= 0;
        int alignement = Math.abs(MathHelper.clamp(this.cap.getAlignmentValue(), this.cap.getAlignmentMinValue(), this.cap.getAlignmentMaxValue()));
        final double ratio;
        if (alignement > 300) {
            ratio = 0.75d + 0.25d * (alignement - 300) * 0.005d;
        } else if (alignement > 150) {
            ratio = 0.5d + 0.25d * (alignement - 150) / 150d;
        } else if (alignement > 50) {
            ratio = 0.25d + 0.25d * (alignement - 50) * 0.01d;
        } else {
            ratio = 0.25d * alignement * 0.02d;
        }
        return isPositive ? ratio : -ratio;
    }

    private void drawPerk(MatrixStack matrixStack, PerkIcon icon) {
        int levelWithBonus = this.cap.getPerkLevelWithBonus(getMinecraft().player, icon.perk);
        boolean isHovered = icon.equals(this.hoveredIcon);
        boolean isMax = this.cap.getPerkLevel(getMinecraft().player, icon.perk) == icon.perk.getLevelMax();
        RenderSystem.pushMatrix();
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        fill(matrixStack, icon.getMinX() - 1, icon.getMinY() - 1, icon.getMaxX() + 1, icon.getMaxY() + 1 + 10, 0xff16163B);
        if (isHovered) {
            int color = isMax ? this.maxColor : 0xff93b7d5;
            hLine(matrixStack, icon.minX - 2, icon.maxX + 1, icon.minY - 2, color);
            hLine(matrixStack,icon.minX - 2, icon.maxX + 1, icon.minY + 27, color);
            vLine(matrixStack, icon.minX - 2, icon.minY - 2, icon.minY + 27, color);
            vLine(matrixStack, icon.minX + 17, icon.minY - 2, icon.minY + 27, color);
        }
        if (levelWithBonus == 0 && !isHovered) {
            fillGradient(matrixStack, icon.getMinX(), icon.getMinY(), icon.getMaxX(), icon.getMaxY(), 0xff333333, 0xff333333);
            RenderSystem.color4f(0.13f, 0.13f, 0.13f, 0.5f);
        } else {
            fillGradient(matrixStack, icon.getMinX(), icon.getMinY(), icon.getMaxX(), icon.getMaxY(), 0xff011f4b, 0xff93b7d5);
            RenderSystem.color4f(1f, 1f, 1f, 1f);
        }
        ResourceLocation texture = icon.perk.getIcon();
        if (texture != null) {
            getMinecraft().getTextureManager().bindTexture(texture);
            blit(matrixStack, icon.getMinX(), icon.getMinY(), 0, 0, this.iconSize, this.iconSize, this.iconSize, this.iconSize);
        }

        boolean hasBonus = icon.perk.getLevelBonus(getMinecraft().player) != 0;
        this.font.drawString(matrixStack, "" + levelWithBonus, icon.getMinX() + (levelWithBonus > 9 ? 2.5f : 5.5f), icon.getMinY() + 18, (hasBonus ? this.bonusColor : (isMax ? this.maxColor : levelWithBonus > 0 ? this.defaultColor : this.disableColor)));

        RenderSystem.popMatrix();
    }

    private void drawPerkTooltip(MatrixStack matrixStack) {
        if (this.hoveredIcon != null) {
            this.hoveredPerkLevel = this.cap.getPerkLevel(getMinecraft().player, this.hoveredIcon.perk);
            int levelWithBonus = this.cap.getPerkLevelWithBonus(getMinecraft().player, hoveredIcon.perk);
            List<String> list = new ArrayList<>();
            list.add(this.hoveredIcon.perk.getClientTranslation());
            String specialInfo = hoveredIcon.perk.getSpecialInfo(levelWithBonus);
            if (!specialInfo.isEmpty()) {
                list.add(specialInfo);
            }
            IntStream.rangeClosed(1, this.hoveredIcon.perk.getLevelMax()).forEach(i -> {
                String info = this.hoveredIcon.perk.getTooltip(i, this.hoveredPerkLevel, levelWithBonus);
                if (!info.isEmpty()) {
                    list.add((this.hoveredPerkLevel >= i ? TextFormatting.WHITE : (hoveredIcon.perk.isEncrypted() ? levelWithBonus >= i : levelWithBonus == i) ? TextFormatting.DARK_PURPLE : TextFormatting.DARK_GRAY).toString() + i + " -> " + LangKey.getClientTranslation(info));
                }
            });
            if (this.hoveredPerkLevel < this.hoveredIcon.perk.getLevelMax()) {
                int cost = this.hoveredIcon.perk.getCost(this.hoveredPerkLevel + 1);
                boolean canUpgrade = this.leftPerkPoints >= cost;
                list.add((canUpgrade ? TextFormatting.AQUA : TextFormatting.RED) + LangKey.MESSAGE_COST.getClientTranslation(cost));
                list.add((canUpgrade ? TextFormatting.BLUE + LangKey.MESSAGE_CLICK_TO_UPGRADE.getClientTranslation() : TextFormatting.RED + LangKey.MESSAGE_CANT_UPGRADE.getClientTranslation()));
            } else {
                list.add(TextFormatting.GOLD + LangKey.MESSAGE_MAX.getClientTranslation());
            }
            drawHoveringText(matrixStack, list, this.hoveredIcon.minX + 10, this.hoveredIcon.minY + 10, this.font);
        }
    }

    private void drawHoveringText(MatrixStack matrixStack, List<String> textLines, int x, int y, FontRenderer font) {
        int maxTextWidth = 200;
        if (!textLines.isEmpty()) {
            RenderSystem.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            RenderSystem.disableLighting();
            RenderSystem.disableDepthTest();
            int tooltipTextWidth = 0;
            for (String textLine : textLines) {
                int textLineWidth = font.getStringWidth(textLine);

                if (textLineWidth > tooltipTextWidth) {
                    tooltipTextWidth = textLineWidth;
                }
            }
            boolean needsWrap = false;
            int titleLinesCount = 1;
            int tooltipX = x + 12;
            if (tooltipX + tooltipTextWidth + 4 > width) {
                tooltipX = x - 16 - tooltipTextWidth;
                if (tooltipX < 4) // if the tooltip doesn't fit on the screen
                {
                    if (x > width / 2) {
                        tooltipTextWidth = x - 12 - 8;
                    } else {
                        tooltipTextWidth = width - 16 - x;
                    }
                    needsWrap = true;
                }
            }
            if (tooltipTextWidth > maxTextWidth) {
                tooltipTextWidth = maxTextWidth;
                needsWrap = true;
            }
            if (needsWrap) {
                int wrappedTooltipWidth = 0;
                List<String> wrappedTextLines = new ArrayList<>();
                for (int i = 0; i < textLines.size(); i++) {
                    String textLine = textLines.get(i);
                    List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);
                    if (i == 0) {
                        titleLinesCount = wrappedLine.size();
                    }
                    for (String line : wrappedLine) {
                        int lineWidth = font.getStringWidth(line);
                        if (lineWidth > wrappedTooltipWidth) {
                            wrappedTooltipWidth = lineWidth;
                        }
                        wrappedTextLines.add(line);
                    }
                }
                tooltipTextWidth = wrappedTooltipWidth;
                textLines = wrappedTextLines;
                if (x > width / 2) {
                    tooltipX = x - 16 - tooltipTextWidth;
                } else {
                    tooltipX = x + 12;
                }
            }
            int tooltipY = y - 12;
            int tooltipHeight = 8;
            if (textLines.size() > 1) {
                tooltipHeight += (textLines.size() - 1) * 10;
                if (textLines.size() > titleLinesCount) {
                    tooltipHeight += 2; // gap between title lines and next lines
                }
            }
            if (tooltipY < 4) {
                tooltipY = 4;
            } else if (tooltipY + tooltipHeight + 4 > this.height) {
                tooltipY = this.height - tooltipHeight - 4;
            }
            final int zLevel = 300;
            int backgroundColor = 0xF0100010;
            int borderColorStart = 0x505000FF;
            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            Helper.fillGradient(tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor, zLevel, false);
            Helper.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor, zLevel, false);
            Helper.fillGradient(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, zLevel, false);
            Helper.fillGradient(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, zLevel, false);
            Helper.fillGradient(tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor, zLevel, false);
            Helper.fillGradient(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd, zLevel, false);
            Helper.fillGradient(tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd, zLevel, false);
            Helper.fillGradient(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart, zLevel, false);
            Helper.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd, zLevel, false);
            int perkLine = 0;
            for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
                String line = textLines.get(lineNumber);
                // font.drawStringWithShadow(line, (float)tooltipX, (float)tooltipY, -1);
                RenderSystem.color4f(1f, 1f, 1f, 1f);
                String[] splits = line.split(" -> ");
                boolean isPerkLine = splits.length > 1;
                if (isPerkLine && this.hoveredIcon.perk.isEncrypted() && perkLine > this.cap.getPerkLevelWithBonus(getMinecraft().player, this.hoveredIcon.perk)) {
                    String subString = splits[0] + " -> ";
                    font.drawString(matrixStack, subString, (float) tooltipX, (float) tooltipY, 0xffE1C87C);
                    FontRenderer standardGalacticFontRenderer = getMinecraft().getFontResourceManager().getFontRenderer(Minecraft.standardGalacticFontRenderer);
                    if (standardGalacticFontRenderer != null) {
                        standardGalacticFontRenderer.drawString(matrixStack, TextFormatting.DARK_GRAY + splits[1], (float) (tooltipX + font.getStringWidth(subString)), (float) tooltipY, 0xffE1C87C);
                    }
                } else {
                    font.drawString(matrixStack, line, (float) tooltipX, (float) tooltipY, 0xffE1C87C);
                }
                if (isPerkLine) {
                    perkLine++;
                }
                if (lineNumber + 1 == titleLinesCount) {
                    tooltipY += 2;
                }
                tooltipY += 10;
            }
            RenderSystem.enableLighting();
            RenderSystem.enableDepthTest();
            //PortingHelper.enableStandardItemLighting();
            RenderSystem.enableRescaleNormal();
        }
    }

    public class PerkIcon {
        public final Perk perk;
        int minX, minY, maxX, maxY;

        PerkIcon(Perk perk) {
            this.perk = perk;
        }

        void setPosition(int x, int y) {
            this.minX = x;
            this.minY = y;
            this.maxX = x + iconSize;
            this.maxY = y + iconSize;
        }

        boolean contains(double x, double y) {
            return x >= this.minX && y >= this.minY && x <= this.maxX && y <= this.maxY + 10;
        }

        int getMinX() {
            return this.minX;
        }

        int getMinY() {
            return this.minY;
        }

        int getMaxX() {
            return this.maxX;
        }

        int getMaxY() {
            return this.maxY;
        }
    }
}
