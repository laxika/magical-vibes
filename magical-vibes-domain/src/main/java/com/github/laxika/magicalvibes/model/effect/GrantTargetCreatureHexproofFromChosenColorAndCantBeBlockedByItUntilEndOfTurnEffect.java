package com.github.laxika.magicalvibes.model.effect;

/** On resolution, choose a color; the target creature gains hexproof from that color and can't be blocked by creatures of that color until end of turn. */
public record GrantTargetCreatureHexproofFromChosenColorAndCantBeBlockedByItUntilEndOfTurnEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
