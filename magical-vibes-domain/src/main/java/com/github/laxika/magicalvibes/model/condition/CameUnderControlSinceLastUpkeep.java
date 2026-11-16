package com.github.laxika.magicalvibes.model.condition;

/**
 * The source permanent came under your control since the beginning of your last upkeep, echo's intervening "if"
 * (CR 702.30a). Read during your upkeep, "your last upkeep" is the one before it: the permanent has been under your
 * control through at most the beginning of this upkeep. "Your" is the controller of the ability being evaluated, so
 * the condition is false while another player controls the source.
 */
public record CameUnderControlSinceLastUpkeep() implements Condition {

    @Override
    public String conditionName() {
        return "came under your control since the beginning of your last upkeep";
    }

    @Override
    public String conditionNotMetReason() {
        return "it has been under your control since before your last upkeep";
    }
}
