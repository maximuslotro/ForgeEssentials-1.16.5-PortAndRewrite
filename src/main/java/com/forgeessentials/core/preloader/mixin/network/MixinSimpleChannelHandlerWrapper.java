package com.forgeessentials.core.preloader.mixin.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.forgeessentials.auth.ModuleAuth;
import com.forgeessentials.core.preloader.asminjector.annotation.Shadow;
import com.forgeessentials.data.v2.DataManager;
import com.forgeessentials.util.output.LoggingHandler;

import io.netty.channel.SimpleChannelInboundHandler;

@Mixin(SimpleChannelHandlerWrapper.class)
public abstract class MixinSimpleChannelHandlerWrapper<REQ extends IMessage, REPLY extends IMessage> extends SimpleChannelInboundHandler<REQ>
{
    @Shadow
    @Final
    private IMessageHandler<? super REQ, ? extends REPLY> messageHandler;

    @Redirect(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;)V", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraftforge/fml/common/network/simpleimpl/IMessageHandler;onMessage(Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;Lnet/minecraftforge/fml/common/network/simpleimpl/MessageContext;)Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;",
            remap = false), remap = false)
    private REPLY redirectNetworkHandler(IMessageHandler iMessageHandler, REQ message, MessageContext ctx)
    {

        ServerPlayerEntity player = ctx.netHandler instanceof NetHandlerPlayServer ? ctx.getServerHandler().player : null;
        if (ctx.side == Dist.CLIENT || !ModuleAuth.isEnabled() || ModuleAuth.isAuthenticated(player) || ModuleAuth.isAllowedMethod(message))
        {
            return messageHandler.onMessage(message, ctx);
        }
        else
        {
            LoggingHandler.felog.debug("Message '{}' from user '{}' ignored because player is not authenticated!", DataManager.getGson().toJson(message),
                    player.getDisplayName());
            return null;
        }
    }
}
