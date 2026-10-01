package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Static P/T boost for creatures controlled by players who chose a named mode as this entered. */
public record BoostCreaturesOfChosenPlayerModeEffect(String mode, int powerBoost, int toughnessBoost,
                                                     Set<Keyword> grantedKeywords)
        implements StaticCreatureBoostEffect {

    public BoostCreaturesOfChosenPlayerModeEffect {
        grantedKeywords = grantedKeywords == null ? Set.of() : Set.copyOf(grantedKeywords);
    }

    public BoostCreaturesOfChosenPlayerModeEffect(String mode, int powerBoost, int toughnessBoost) {
        this(mode, powerBoost, toughnessBoost, Set.of());
    }

    @Override
    public Set<Keyword> grantedKeywords() {
        return grantedKeywords;
    }

    @Override
    public GrantScope scope() {
        return GrantScope.ALL_CREATURES_INCLUDING_SELF;
    }

    @Override
    public PermanentPredicate filter() {
        return null;
    }
}
