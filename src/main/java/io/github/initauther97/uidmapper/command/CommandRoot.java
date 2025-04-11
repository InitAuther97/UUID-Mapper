package io.github.initauther97.uidmapper.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.initauther97.uidmapper.Messages;
import io.github.initauther97.uidmapper.UUIDMapper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.UuidArgument;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.UUID;

public class CommandRoot {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("uidmapper").then(
                        LiteralArgumentBuilder.<CommandSourceStack>literal("set").then(
                                RequiredArgumentBuilder.<CommandSourceStack, String>argument("username", StringArgumentType.string()).then(
                                        RequiredArgumentBuilder.<CommandSourceStack, UUID>argument("uuid", UuidArgument.uuid())
                                                .executes(CommandRoot::setUuid)
                                )
                        )
                ).then(
                        LiteralArgumentBuilder.<CommandSourceStack>literal("get").then(
                                RequiredArgumentBuilder.<CommandSourceStack, String>argument("username", StringArgumentType.string())
                                        .executes(CommandRoot::getUuid)
                        ).executes(CommandRoot::getAll)
                ).then(
                        LiteralArgumentBuilder.<CommandSourceStack>literal("remove").then(
                                RequiredArgumentBuilder.<CommandSourceStack, String>argument("username", StringArgumentType.string())
                                        .executes(CommandRoot::remove)
                        )
                ).then(
                        LiteralArgumentBuilder.<CommandSourceStack>literal("removeAll")
                                .executes(CommandRoot::removeAll)
                ).then(
                        LiteralArgumentBuilder.<CommandSourceStack>literal("override")
                                .executes(CommandRoot::override)
                )
        );
    }

    public static int setUuid(CommandContext<CommandSourceStack> ctx) {
        final var instance = UUIDMapper.getInstance();
        if (instance.broken) {
            ctx.getSource().sendFailure(Messages.NEED_OVERRIDE);
            return 1;
        }
        final var username = StringArgumentType.getString(ctx, "username");
        final var uuid = UuidArgument.getUuid(ctx, "uuid");
        instance.mapping.put(username, uuid);
        instance.dirty = true;
        ctx.getSource().sendSuccess(() -> Messages.setSuccess(username, uuid), true);
        return 0;
    }

    public static int getUuid(CommandContext<CommandSourceStack> ctx) {
        final var instance = UUIDMapper.getInstance();
        if (instance.broken) {
            ctx.getSource().sendFailure(Messages.NEED_OVERRIDE);
            return 1;
        }
        final var username = StringArgumentType.getString(ctx, "username");
        final var uuid = instance.mapping.get(username);
        if (uuid != null) {
            ctx.getSource().sendSystemMessage(Messages.mappingResult(username, uuid));
        } else {
            ctx.getSource().sendFailure(Messages.MAPPING_NOT_FOUND);
        }
        return 0;
    }

    public static int getAll(CommandContext<CommandSourceStack> ctx) {
        if (UUIDMapper.getInstance().broken) {
            ctx.getSource().sendFailure(Messages.NEED_OVERRIDE);
            return 1;
        }
        final var source = ctx.getSource();
        final var mapping = UUIDMapper.getInstance().mapping;
        source.sendSystemMessage(Messages.beginUidList(mapping.size()));
        final var nth = new MutableInt();
        mapping.forEach((user, uid) -> {
            nth.increment();
            source.sendSystemMessage(Messages.mappingAtN(nth.intValue(), user, uid));
        });
        return 0;
    }

    public static int remove(CommandContext<CommandSourceStack> ctx) {
        final var instance = UUIDMapper.getInstance();
        final var source = ctx.getSource();
        if (instance.broken) {
            source.sendFailure(Messages.NEED_OVERRIDE);
            return 1;
        }
        final var username = StringArgumentType.getString(ctx, "username");
        final var previous = instance.mapping.remove(username);
        if (previous != null) {
            source.sendSuccess(() -> Messages.removeSuccess(username, previous), true);
            return 0;
        } else {
            source.sendFailure(Messages.REMOVE_NOT_FOUND);
            return 1;
        }
    }

    public static int removeAll(CommandContext<CommandSourceStack> ctx) {
        final var instance = UUIDMapper.getInstance();
        final var source = ctx.getSource();
        if (instance.broken) {
            source.sendFailure(Messages.NEED_OVERRIDE);
            return 1;
        }
        for (String username: instance.mapping.keySet()) {
            var previous = instance.mapping.remove(username);
            source.sendSuccess(() -> Messages.removeSuccess(username, previous), true);
        }
        return 0;
    }

    public static int override(CommandContext<CommandSourceStack> ctx) {
        final var source = ctx.getSource();
        final var instance = UUIDMapper.getInstance();
        if (instance.broken) {
            instance.broken = false;
            instance.dirty = true;
            source.sendSuccess(() -> Messages.OVERRIDE_SUCCESS, true);
            return 0;
        } else {
            source.sendFailure(Messages.NO_NEED_OVERRIDE);
            return 1;
        }
    }
}
