package com.github.laxika.magicalvibes.model.effect;

/**
 * Static blocking restriction: this creature can't block attackers whose effective power is equal to or
 * greater than {@code minAttackerPower} (Ironclaw Orcs, {@code (2)}). A hard restriction with no way
 * around it — unlike {@code CantBlockHighPowerCreaturesUnlessPaysEffect} (a per-block mana tax) and
 * {@code CantBlockCreaturesWithPowerGreaterOrEqualToOwnToughnessEffect} (self-referential to the
 * blocker's toughness). Read at declare-blockers time by {@code GameQueryService#findBlockDenial}.
 */
public record CantBlockCreaturesWithPowerAtLeastEffect(int minAttackerPower) implements BlockingRestrictionEffect {

    @Override
    public Integer cantBlockCreaturesWithPowerAtLeast() {
        return minAttackerPower;
    }
}
