package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player votes for denial or duplication. If denial
 * gets more votes, the target spell is countered; otherwise it is copied.
 */
public record VoteForDenialOrDuplicationEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
