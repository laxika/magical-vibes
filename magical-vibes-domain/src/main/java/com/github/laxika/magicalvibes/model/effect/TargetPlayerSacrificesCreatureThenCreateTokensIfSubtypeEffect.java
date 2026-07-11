package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * "Target player sacrifices a creature of their choice. If a [subtype] is sacrificed this way, that
 * player creates [tokens]." (Warren Weirding.)
 *
 * <p>The targeted player chooses which of their creatures to sacrifice. Only if the sacrificed
 * creature has {@code requiredSubtype} (checked on last-known information, so type-adding effects
 * such as changeling count) does that same player create tokens from {@code tokenTemplate} — the
 * template already encodes count, characteristics, and any keywords granted until end of turn.
 *
 * @param requiredSubtype subtype the sacrificed creature must have for the follow-up to happen
 * @param tokenTemplate   tokens the sacrificing player creates when the condition is met
 */
public record TargetPlayerSacrificesCreatureThenCreateTokensIfSubtypeEffect(
        CardSubtype requiredSubtype, CreateTokenEffect tokenTemplate) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
