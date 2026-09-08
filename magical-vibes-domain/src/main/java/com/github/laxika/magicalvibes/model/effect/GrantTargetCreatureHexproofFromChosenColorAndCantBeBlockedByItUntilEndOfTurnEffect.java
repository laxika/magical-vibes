package com.github.laxika.magicalvibes.model.effect;

/** On resolution, choose a color; the target creature gains hexproof from that color and can't be blocked by creatures of that color until end of turn. */
public record GrantTargetCreatureHexproofFromChosenColorAndCantBeBlockedByItUntilEndOfTurnEffect(
        boolean selfTargeting)
        implements CardEffect {

    public GrantTargetCreatureHexproofFromChosenColorAndCantBeBlockedByItUntilEndOfTurnEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return selfTargeting
                ? new TargetSpec(null, false, null, true, 1)
                : TargetSpec.benign(TargetPredicates.creature());
    }
}
