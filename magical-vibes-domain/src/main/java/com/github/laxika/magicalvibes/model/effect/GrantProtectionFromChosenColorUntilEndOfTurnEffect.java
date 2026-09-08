package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants the target creature protection from the source permanent's chosen color until end of turn.
 * The source's last-known permanent snapshot is used when the source left the battlefield as a cost.
 */
public record GrantProtectionFromChosenColorUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
