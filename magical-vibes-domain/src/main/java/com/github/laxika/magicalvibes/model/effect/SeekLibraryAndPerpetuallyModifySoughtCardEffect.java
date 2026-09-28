package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;
import java.util.Set;

/** Seeks one matching card into hand and applies perpetual card-identity modifications to it. */
public record SeekLibraryAndPerpetuallyModifySoughtCardEffect(
        CardPredicate filter,
        int maxManaValue,
        Set<Keyword> keywords,
        int genericCastCostReduction,
        EffectSlot triggeredAbilitySlot,
        CardEffect triggeredAbility) implements CardEffect {

    public SeekLibraryAndPerpetuallyModifySoughtCardEffect {
        Objects.requireNonNull(filter, "filter");
        if (maxManaValue < 0) {
            throw new IllegalArgumentException("maxManaValue cannot be negative");
        }
        keywords = Set.copyOf(keywords);
        if (genericCastCostReduction < 0) {
            throw new IllegalArgumentException("genericCastCostReduction cannot be negative");
        }
        if ((triggeredAbilitySlot == null) != (triggeredAbility == null)) {
            throw new IllegalArgumentException(
                    "Triggered ability slot and effect must be provided together");
        }
    }
}
