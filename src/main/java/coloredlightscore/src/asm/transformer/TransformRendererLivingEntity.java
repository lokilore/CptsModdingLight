package coloredlightscore.src.asm.transformer;

import coloredlightscore.src.asm.ColoredLightsCoreLoadingPlugin;
import coloredlightscore.src.asm.transformer.core.MethodTransformer;
import coloredlightscore.src.asm.transformer.core.NameMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

/**
 * Created by Murray on 12/5/2014.
 */
public class TransformRendererLivingEntity extends MethodTransformer {

    private static final String unobfLightmapTexUnit = "lightmapTexUnit";
    private static final String obfLightmapTexUnit = "field_77476_b";
    private static final String unobfEntityRenderer = "entityRenderer";
    private static final String obfEntityRenderer = "field_71460_t";
    private static final String unobfEnableLightmap = "enableLightmap";
    private static final String obfEnableLightmap = "func_78463_b";
    private static final String unobfGetMinecraft = "getMinecraft";
    private static final String obfGetMinecraft = "func_71410_x";

    @Override
    protected boolean transforms(ClassNode classNode, MethodNode methodNode) {
        if (NameMapper.getInstance().isMethod(methodNode, classNode.name, "doRender (Lnet/minecraft/entity/EntityLivingBase;DDDFF)V")) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean transform(ClassNode classNode, MethodNode methodNode) {
        if (NameMapper.getInstance().isMethod(methodNode, classNode.name, "doRender (Lnet/minecraft/entity/EntityLivingBase;DDDFF)V")) {
            return transformDoRender(methodNode);
        }
        return false;
    }

    @Override
    protected boolean transforms(String className) {
        return className.equals("net.minecraft.client.renderer.entity.RendererLivingEntity");
    }

    private boolean transformDoRender(MethodNode methodNode) {
        ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
        boolean hasFoundFirstLightmap = false;
        boolean hasFoundSecondLightmap = false;
        AbstractInsnNode insn;
        String lightmapTexUnit = ColoredLightsCoreLoadingPlugin.MCP_ENVIRONMENT ? unobfLightmapTexUnit : obfLightmapTexUnit;
        String entityRenderer = ColoredLightsCoreLoadingPlugin.MCP_ENVIRONMENT ? unobfEntityRenderer : obfEntityRenderer;
        String enableLightmap = ColoredLightsCoreLoadingPlugin.MCP_ENVIRONMENT ? unobfEnableLightmap : obfEnableLightmap;
        String getMinecraft = ColoredLightsCoreLoadingPlugin.MCP_ENVIRONMENT ? unobfGetMinecraft : obfGetMinecraft;
        while (iterator.hasNext() && !hasFoundFirstLightmap) {
            insn = iterator.next();
            if (insn.getOpcode() == Opcodes.GETSTATIC && ((FieldInsnNode)insn).name.equals(lightmapTexUnit)) {
                hasFoundFirstLightmap = true;
            }
        }
        if (hasFoundFirstLightmap) {
            insn = iterator.next(); // INVOKESTATIC net/minecraft/client/renderer/OpenGlHelper.setActiveTexture (I)V
            iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/client/Minecraft", getMinecraft, "()Lnet/minecraft/client/Minecraft;"));
            iterator.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", entityRenderer, "Lnet/minecraft/client/renderer/EntityRenderer;"));
            iterator.add(new InsnNode(Opcodes.DCONST_0));
            iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "coloredlightscore/src/helper/CLTessellatorHelper", "isProgramInUse", "()Z"));
            iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "coloredlightscore/src/helper/CLEntityRendererHelper", "disableLightmap", "(Lnet/minecraft/client/renderer/EntityRenderer;DZ)V"));

            while (iterator.hasNext() && !hasFoundSecondLightmap) {
                insn = iterator.next();
                if (insn.getOpcode() == Opcodes.GETSTATIC && ((FieldInsnNode) insn).name.equals(lightmapTexUnit)) {
                    hasFoundSecondLightmap = true;
                }
            }
            if (hasFoundSecondLightmap) {
                insn = iterator.next(); // INVOKESTATIC net/minecraft/client/renderer/OpenGlHelper.setActiveTexture (I)V
                iterator.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/minecraft/client/Minecraft", getMinecraft, "()Lnet/minecraft/client/Minecraft;"));
                iterator.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", entityRenderer, "Lnet/minecraft/client/renderer/EntityRenderer;"));
                iterator.add(new InsnNode(Opcodes.DCONST_0));
                iterator.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/client/renderer/EntityRenderer", enableLightmap, "(D)V"));
            }
        }

        return hasFoundFirstLightmap && hasFoundSecondLightmap;
    }
}
