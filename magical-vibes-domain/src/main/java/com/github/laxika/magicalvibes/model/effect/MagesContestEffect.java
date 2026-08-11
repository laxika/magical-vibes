package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller and the controller of the targeted spell bid life; the high bidder loses that
 * much life, and Mages' Contest counters the targeted spell if the Mages' Contest controller won.
 */
public record MagesContestEffect() implements CounterSpellingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
