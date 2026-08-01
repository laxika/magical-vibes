package com.github.laxika.magicalvibes.model.condition;

/**
 * The spell was cast for its overload cost (CR 702.96a). Paired with
 * {@link com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect} to express the
 * text change of CR 702.96a: the base effect is the printed "target" version, the upgraded effect
 * is the same effect with every "target" read as "each". Per CR 702.96b an overloaded spell
 * requires no targets at all, so the upgraded branch must be a non-targeting (mass) effect.
 */
public record Overloaded() implements Condition {

    @Override
    public String conditionName() {
        return "overloaded";
    }

    @Override
    public String conditionNotMetReason() {
        return "spell was not overloaded";
    }
}
