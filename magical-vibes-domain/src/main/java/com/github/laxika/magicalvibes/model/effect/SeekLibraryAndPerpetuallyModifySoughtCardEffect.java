package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.AbilityEffects;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;
import java.util.Set;

/** Seeks one matching card into hand and applies perpetual card-identity modifications to it. */
public record SeekLibraryAndPerpetuallyModifySoughtCardEffect(
        CardPredicate filter,
        int maxManaValue,
        Set<Keyword> keywords,
        int genericCastCostReduction,
        Ability grantedAbility) implements CardEffect {

    public SeekLibraryAndPerpetuallyModifySoughtCardEffect {
        Objects.requireNonNull(filter, "filter");
        if (maxManaValue < 0) {
            throw new IllegalArgumentException("maxManaValue cannot be negative");
        }
        keywords = Set.copyOf(keywords);
        if (genericCastCostReduction < 0) {
            throw new IllegalArgumentException("genericCastCostReduction cannot be negative");
        }
    }

    /** The effect of {@link #grantedAbility}, or {@code null} when there is none. */
    public CardEffect triggeredAbility() {
        return grantedAbility == null ? null : AbilityEffects.effectsOf(grantedAbility).getFirst();
    }
}
