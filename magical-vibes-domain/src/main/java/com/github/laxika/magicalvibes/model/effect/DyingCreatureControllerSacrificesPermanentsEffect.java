package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker for "whenever a [qualifying] creature dies, that creature's controller sacrifices
 * {@code count} permanent(s) matching {@code filter} of their choice" (Earthlink).
 * <p>
 * Placed on the {@code ON_ANY_CREATURE_DIES} slot (optionally wrapped in a
 * {@link TriggeringCardConditionalEffect} to restrict which creatures qualify). The trigger collector
 * stacks a {@link SacrificePermanentsEffect} with {@link SacrificeRecipient#CONTROLLER} under the
 * dying creature's controller — who may be an opponent of this permanent's controller — mirroring
 * {@link DyingCreatureControllerDiscardsCardEffect}.
 */
public record DyingCreatureControllerSacrificesPermanentsEffect(
        int count,
        PermanentPredicate filter) implements CardEffect {
}
