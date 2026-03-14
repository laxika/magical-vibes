package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

public record DestroyAllPermanentsEffect(
        Set<CardType> targetTypes,
        Set<CardType> excludedTypes,
        boolean onlyOpponents,
        boolean cannotBeRegenerated,
        PermanentPredicate filter
) implements CardEffect {

    public DestroyAllPermanentsEffect {
        targetTypes = targetTypes.isEmpty() ? Set.of() : Set.copyOf(targetTypes);
        excludedTypes = excludedTypes == null || excludedTypes.isEmpty() ? Set.of() : Set.copyOf(excludedTypes);
    }

    public DestroyAllPermanentsEffect(Set<CardType> targetTypes) {
        this(targetTypes, Set.of(), false, false, null);
    }

    public DestroyAllPermanentsEffect(Set<CardType> targetTypes, boolean cannotBeRegenerated) {
        this(targetTypes, Set.of(), false, cannotBeRegenerated, null);
    }

    public DestroyAllPermanentsEffect(Set<CardType> targetTypes, boolean onlyOpponents, boolean cannotBeRegenerated) {
        this(targetTypes, Set.of(), onlyOpponents, cannotBeRegenerated, null);
    }

    public DestroyAllPermanentsEffect(Set<CardType> targetTypes, boolean onlyOpponents, boolean cannotBeRegenerated, PermanentPredicate filter) {
        this(targetTypes, Set.of(), onlyOpponents, cannotBeRegenerated, filter);
    }

    /**
     * Creates an effect that destroys all permanents except those of the excluded types.
     * Use this for cards like "destroy all nonland permanents".
     */
    public static DestroyAllPermanentsEffect excludingTypes(Set<CardType> excludedTypes) {
        return new DestroyAllPermanentsEffect(Set.of(), excludedTypes, false, false, null);
    }

    public static DestroyAllPermanentsEffect excludingTypes(Set<CardType> excludedTypes, boolean cannotBeRegenerated) {
        return new DestroyAllPermanentsEffect(Set.of(), excludedTypes, false, cannotBeRegenerated, null);
    }
}
