package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker for "whenever a [qualifying] creature dies, its controller discards a card" (Bereavement).
 * <p>
 * Placed on the {@code ON_ANY_CREATURE_DIES} slot (usually wrapped in a
 * {@link TriggeringCardConditionalEffect} to restrict which creatures qualify). The trigger
 * collector stacks a mandatory single-card discard under the dying creature's controller (who may be
 * an opponent of this permanent's controller), mirroring
 * {@link DyingCreatureControllerMayDrawCardEffect} but mandatory and for a discard.
 */
public record DyingCreatureControllerDiscardsCardEffect() implements CardEffect {
}
