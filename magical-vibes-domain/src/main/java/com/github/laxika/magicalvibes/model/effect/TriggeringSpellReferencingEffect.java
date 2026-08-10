package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability marker for effects that act on the spell or ability that caused them rather than on a
 * chosen target ("that spell's controller loses 5 life"). Normally these piggyback on a
 * counterspell's target, but when one fires from a becomes-target trigger there is no chosen target,
 * so the trigger collector points the queued ability at the triggering object's card in the
 * {@code STACK} zone.
 */
public interface TriggeringSpellReferencingEffect extends CardEffect {
}
