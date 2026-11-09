package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability marker for effects that act on the spell or ability that caused them rather than on a
 * chosen target ("that spell's controller loses 5 life"). Normally these piggyback on a
 * counterspell's target. A becomes-the-target ability reads the triggering spell or ability its
 * collector records on every entry, whatever the effects are; the "when you cast this spell"
 * collector still records the spell by this marker.
 */
public interface TriggeringSpellReferencingEffect extends CardEffect {
}
