package io.github.initauther97.uidmapper;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.util.UUID;

public final class Messages {
    public static final Component INFORM_JOINER = Component
            .literal("Please beware that UUID forwarding is enabled on this server!")
            .withStyle(ChatFormatting.YELLOW);

    public static final Component NEED_OVERRIDE = Component
            .literal("Failed to read UUID mapping. To override it, run ")
            .withStyle(ChatFormatting.RED)
            .append(Component.literal("override")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE)
                    .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("/uidmapper override"))))
                    .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uidmapper override"))));

    public static final Component NO_NEED_OVERRIDE = Component
            .literal("No need to override.")
            .withStyle(ChatFormatting.RED);

    public static final Component OVERRIDE_SUCCESS = Component
            .literal("Successfully override damaged mappings.")
            .withStyle(ChatFormatting.GREEN);

    public static final Component REMOVE_NOT_FOUND = Component
            .literal("Remove failed: mapping not found.")
            .withStyle(ChatFormatting.RED);

    public static final Component SAVE_FAILED = Component
            .literal("[FATAL] Cannot save UUID mapping! Please refer to logs for more detail.")
            .withStyle(ChatFormatting.DARK_RED);

    public static final Component SAVE_SUCCESS = Component
            .literal("Successfully saved UUID mappings")
            .withStyle(ChatFormatting.GREEN);

    private static Component copyable(String text) {
        return Component.literal(text).withStyle(style ->
                style.withUnderlined(true)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)));
    }

    public static Component setSuccess(String username, UUID uid) {
        return Component.literal(String.format("Successfully set UUID for %s to ", username))
                .append(copyable(uid.toString()))
                .withStyle(ChatFormatting.GREEN);
    }

    public static Component setFailed(String reason) {
        return Component.literal("Cannot set uid mapping: " + reason)
                .withStyle(ChatFormatting.RED);
    }

    public static final Component MAPPING_NOT_FOUND = Component
            .literal("Mapping not found")
            .withStyle(ChatFormatting.RED);

    public static Component mappingResult(String username, UUID uid) {
        return Component.literal(username + " -> ")
                .append(copyable(uid.toString()));
    }

    public static Component beginUidList(int count) {
        return Component.literal("There are "+count+" existing uid mappings:");
    }

    public static Component mappingAtN(int nth, String username, UUID uid) {
        return Component.literal(nth + ": " + username + " -> ")
                .append(copyable(uid.toString()));
    }

    public static Component removeSuccess(String username, UUID uid) {
        return Component.literal(String.format("Successfully removed mapping for %s, previously to ", username))
                .append(copyable(uid.toString()))
                .withStyle(ChatFormatting.GREEN);
    }

    public static Component informEveryone(GameProfile joiner) {
        return Component.literal(String.format("Player %s joined in with redirected uuid ", joiner.getName()))
                .append(copyable(joiner.getId().toString()))
                .withStyle(ChatFormatting.GREEN);
    }
}
