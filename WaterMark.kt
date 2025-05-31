package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.ccbluex.liquidbounce.LiquidBounce.moduleManager
import net.ccbluex.liquidbounce.config.TextValue
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.GlowUtils
import net.ccbluex.liquidbounce.utils.attack.CPSCounter
import net.ccbluex.liquidbounce.utils.client.ServerUtils
import net.ccbluex.liquidbounce.utils.extensions.getPing
import net.ccbluex.liquidbounce.utils.inventory.SilentHotbar
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawImage
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRoundedRect
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil
import net.ccbluex.liquidbounce.utils.render.shader.Background
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting
import net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting
import net.minecraft.item.ItemBlock
import net.minecraft.util.ResourceLocation
import net.ccbluex.liquidbounce.utils.render.shader.shaders.GlowShader
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.max


object WaterMark : Module("WaterMark", Category.RENDER) {
    private val ClientName by text("ClientName", "Opai")
    private val animationSpeed by float("AnimationSpeed", 0.2F, 0.05F..1F)
    private val animationBackgroundSpeed2 by float("AnimationBackgroundSpeed",0.4F,0.05F..1F)
    private val ColorA_ by int("Red",255,0..255)
    private val ColorB_ by int("Green",255,0..255)
    private val ColorC_ by int("Blue",255,0..255)
    private val ShadowCheck by boolean("Shadow",false)
    private val shadowStrengh by int("ShadowStrength", 1, 1..2)

    private val versionNameUp = "beta"
    private val versionNameDown = "v2.0-beta.7"

    enum class State {
        Normal,
        Scaffold
    }

    val progressLen = 120F
    var ProgressBarAnimationWidth = progressLen

    private var scaledScreen = ScaledResolution(mc)
    private var width = scaledScreen.scaledWidth
    private var height = scaledScreen.scaledHeight
    private var island_State = State.Normal
    private var start_y = (height/9).toFloat()

    private var Anim_BaseStartX = (width/2).toFloat()
    private var Anim_BaseEndX = Anim_BaseStartX

    val onRender2D = handler<Render2DEvent>{
        scaledScreen = ScaledResolution(mc)
        width = scaledScreen.scaledWidth
        height = scaledScreen.scaledHeight
        island_State = State.Normal
        start_y = (height/9).toFloat()
        if (moduleManager.getModule("Scaffold")?.state == true) {
            island_State = State.Scaffold
        }else{
            island_State = State.Normal
        }
        when (island_State) {
            State.Normal -> drawNormal()
            State.Scaffold -> drawScaffold()
        }
    }
    //offset : 2F
    //start_y + 27F
    //offset_2 = 9.5F
    // up:4.5 down: 14F middle:9
    private fun drawNormal() {
        val serverip = ServerUtils.remoteIp
        val playerPing = "${mc.thePlayer.getPing()}ms"
        val textWidth = Fonts.fontSemibold40.getStringWidth(ClientName)

        val ColorAL = Color(ColorA_, ColorB_, ColorC_,255)
        val imageLen = 21F
        val containerToUiDistance = 2F
        val uiToUIDistance = 4F
        val textBar2 = max(Fonts.fontSemibold40.getStringWidth(versionNameUp),Fonts.fontSemibold35.getStringWidth(versionNameDown))
        val textBar3 = max(Fonts.fontSemibold40.getStringWidth(serverip),Fonts.fontSemibold35.getStringWidth(playerPing))
        val LineWidth = 2F
        val fastLen1 = containerToUiDistance+imageLen+uiToUIDistance
        val allLen = fastLen1+textWidth+uiToUIDistance+LineWidth+uiToUIDistance+textBar2+uiToUIDistance+LineWidth+uiToUIDistance+textBar3+containerToUiDistance
        val startX = width/2-allLen/2

        Anim_BaseStartX = AnimationUtil.base(Anim_BaseStartX.toDouble(),startX.toDouble(), animationBackgroundSpeed2.toDouble()).toFloat().coerceAtLeast(0f)
        Anim_BaseEndX = AnimationUtil.base(Anim_BaseEndX.toDouble(),(allLen+startX).toDouble(), animationBackgroundSpeed2.toDouble()).toFloat().coerceAtLeast(0f)

        drawRoundedRect(Anim_BaseStartX,start_y, Anim_BaseEndX, start_y+27F,Color(0,0,0,120).rgb,13F)
        ShowShadow(startX,start_y,allLen, 27F)

        drawImage(ResourceLocation("${CLIENT_NAME.lowercase()}/logo_icon.png"), startX+containerToUiDistance+2F, start_y+4F, 19, 19,ColorAL)//23F, 23F
        Fonts.fontSemibold40.drawString(ClientName,startX+fastLen1,start_y+9F,ColorAL.rgb,false)

        Fonts.fontSemibold40.drawString("|",startX+fastLen1+textWidth+uiToUIDistance-1F,start_y+9F,Color(120,120,120,250).rgb,false)

        Fonts.fontSemibold40.drawString(versionNameUp,startX+fastLen1+textWidth+uiToUIDistance+LineWidth+uiToUIDistance,start_y+4.5F,Color(255,255,255,255).rgb,false)
        Fonts.fontSemibold35.drawString(versionNameDown,startX+fastLen1+textWidth+uiToUIDistance+LineWidth+uiToUIDistance,start_y+14F,Color(170,170,170,170).rgb,false)

        Fonts.fontSemibold40.drawString("|",startX+fastLen1+textWidth+uiToUIDistance+LineWidth+uiToUIDistance+textBar2+uiToUIDistance-1F,start_y+9F,Color(120,120,120,250).rgb,false)

        Fonts.fontSemibold40.drawString(serverip,startX+fastLen1+textWidth+uiToUIDistance+LineWidth+uiToUIDistance+textBar2+uiToUIDistance+LineWidth+uiToUIDistance,start_y+4.5F,Color(255,255,255,255).rgb,false)
        Fonts.fontSemibold35.drawString(playerPing,startX+fastLen1+textWidth+uiToUIDistance+LineWidth+uiToUIDistance+textBar2+uiToUIDistance+LineWidth+uiToUIDistance,start_y+14F,Color(170,170,170,170).rgb,false)

    }
    private fun drawScaffold() {
        val stack = mc.thePlayer?.inventory?.getStackInSlot(SilentHotbar.currentSlot)
        val shouldRender = stack?.item is ItemBlock
        val colorAL1 = Color(255,255,255,255)
        val colorAL2 = Color(0,0,0,200)
        val progressLen_height = 2F
        val imageLen = 23F
        val offsetLen = 2F
        val blockAmount = stack?.stackSize ?: 0
        val CPS = CPSCounter.getCPS(CPSCounter.MouseButton.RIGHT)
        val countWidth = Fonts.fontSemibold40.getStringWidth("$blockAmount blocks")
        val percentProLen = progressLen/64
        val allLen = offsetLen+imageLen+offsetLen+progressLen+offsetLen+2F+countWidth+offsetLen
        val startXScaffold=((width/2)-(allLen/2))

        val progressLenReal2 = offsetLen+imageLen+offsetLen+percentProLen*blockAmount
        ProgressBarAnimationWidth = AnimationUtil.base(ProgressBarAnimationWidth.toDouble(),progressLenReal2.toDouble(), animationSpeed.toDouble()).toFloat().coerceAtLeast(0f)
        Anim_BaseStartX = AnimationUtil.base(Anim_BaseStartX.toDouble(),startXScaffold.toDouble(), animationBackgroundSpeed2.toDouble()).toFloat().coerceAtLeast(0f)
        Anim_BaseEndX = AnimationUtil.base(Anim_BaseEndX.toDouble(),(allLen+startXScaffold).toDouble(), animationBackgroundSpeed2.toDouble()).toFloat().coerceAtLeast(0f)

        drawRoundedRect(Anim_BaseStartX-1F,start_y, Anim_BaseEndX+1F, start_y+27F,Color(0,0,0,120).rgb,13F)
        ShowShadow(startXScaffold,start_y,allLen, 27F)

        drawRoundedRect(startXScaffold+offsetLen+imageLen+offsetLen, start_y+27F/2-progressLen_height/2,startXScaffold+offsetLen+imageLen+offsetLen+progressLen,start_y+27F/2+progressLen_height/2,colorAL2.rgb,3F)
        drawRoundedRect(startXScaffold+offsetLen+imageLen+offsetLen, start_y+27F/2-progressLen_height/2,startXScaffold+ ProgressBarAnimationWidth,start_y+27F/2+progressLen_height/2,colorAL1.rgb,3F)

        Fonts.fontSemibold40.drawString("$blockAmount blocks",startXScaffold+offsetLen+imageLen+offsetLen+progressLen+offsetLen+1F,start_y+4.5F,Color.WHITE.rgb)
        Fonts.fontSemibold35.drawString("${CPS}cps",startXScaffold+offsetLen+imageLen+offsetLen+progressLen+offsetLen+1F,start_y+14F,Color(140,140,140,255).rgb)

        glPushMatrix()
        enableGUIStandardItemLighting()
        if (mc.currentScreen is GuiHudDesigner) glDisable(GL_DEPTH_TEST)
        if (shouldRender) {
            mc.renderItem.renderItemAndEffectIntoGUI(stack, (startXScaffold+offsetLen+4).toInt(), (offsetLen+start_y+3).toInt())
        }
        disableStandardItemLighting()
        enableAlpha()
        disableBlend()
        disableLighting()
        if (mc.currentScreen is GuiHudDesigner) glEnable(GL_DEPTH_TEST)
        glPopMatrix()
    }
    private fun ShowShadow(startX: Float,startY: Float,width: Float,height:Float){
        if (ShadowCheck) {
            GlowUtils.drawGlow(
                startX, startY,
                width, height,
                (shadowStrengh * 13F).toInt(),
                Color(0, 0, 0, 120)
            )
        }
    }
}