package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Pays any amount of energy and grants indestructible until end of turn to your creatures
 * whose power equals the amount paid. */
public record PayAnyAmountOfEnergyToGrantIndestructibleToOwnCreaturesWithEqualPowerEffect()
        implements KeywordGrantingEffect {

    @Override
    public Set<Keyword> keywords() {
        return Set.of(Keyword.INDESTRUCTIBLE);
    }

    @Override
    public GrantScope scope() {
        return GrantScope.ALL_OWN_CREATURES;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}
