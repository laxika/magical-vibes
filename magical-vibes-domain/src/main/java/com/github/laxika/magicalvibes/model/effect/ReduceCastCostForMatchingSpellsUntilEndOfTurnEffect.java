package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.Set;

/**
 * Creates a temporary reduction for matching spells cast by the effect's controller. The amount
 * is evaluated once as this effect resolves, then remains fixed for the duration.
 */
public record ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(CardPredicate predicate,
                                                                  DynamicAmount amount,
                                                                  Set<Zone> sourceZones,
                                                                  boolean faceDownOnly)
        implements CardEffect {

    public ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect {
        sourceZones = Set.copyOf(sourceZones);
    }

    public ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(CardPredicate predicate, int amount) {
        this(predicate, new Fixed(amount), Set.of(), false);
    }

    public ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(CardPredicate predicate, int amount,
                                                                boolean faceDownOnly) {
        this(predicate, new Fixed(amount), Set.of(), faceDownOnly);
    }

    public ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(CardPredicate predicate, int amount,
                                                                Set<Zone> sourceZones) {
        this(predicate, new Fixed(amount), sourceZones, false);
    }

    public ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(CardPredicate predicate,
                                                                DynamicAmount amount) {
        this(predicate, amount, Set.of(), false);
    }

    public ReduceCastCostForMatchingSpellsUntilEndOfTurnEffect(CardPredicate predicate,
                                                                DynamicAmount amount,
                                                                boolean faceDownOnly) {
        this(predicate, amount, Set.of(), faceDownOnly);
    }
}
