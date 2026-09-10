package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessGreaterThanPowerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

/**
 * Dynamic global restriction that lasts until end of turn. A creature whose current toughness is
 * greater than its current power cannot block any attacker.
 */
public record CreaturesWithToughnessGreaterThanPowerCantBlockThisTurnEffect()
        implements BlockingRestrictionEffect {

    @Override
    public PermanentPredicate globalCantBlockBlockerMatcher() {
        return new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentToughnessGreaterThanPowerPredicate()));
    }

    @Override
    public PermanentPredicate globalCantBlockAttackerMatcher() {
        return new PermanentTruePredicate();
    }

    @Override
    public String globalCantBlockDescription() {
        return "Creatures with toughness greater than power can't block this turn";
    }
}
