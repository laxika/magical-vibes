package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever a creature an opponent controls enters, you may attach this Aura to that creature."
 *
 * <p>Trigger-materialising marker for {@code ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD}. Unlike
 * {@link AttachSourceAuraToTargetCreatureEffect} this does not target — "that creature" is the one
 * that just entered. The enter collector resolves the entering permanent and queues a
 * {@code MayEffect(AttachSourceAuraToTargetCreatureEffect)} with {@code targetId} set to the
 * entering creature and {@code sourcePermanentId} set to this Aura. Used by Prison Term.
 */
public record AttachSourceAuraToEnteringCreatureEffect() implements CardEffect {
}
