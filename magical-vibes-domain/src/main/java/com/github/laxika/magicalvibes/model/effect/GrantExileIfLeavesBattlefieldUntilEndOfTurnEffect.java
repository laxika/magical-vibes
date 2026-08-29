package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the target creature an end-of-turn replacement effect that exiles it instead of moving it
 * to any other zone when it leaves the battlefield.
 */
public record GrantExileIfLeavesBattlefieldUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
