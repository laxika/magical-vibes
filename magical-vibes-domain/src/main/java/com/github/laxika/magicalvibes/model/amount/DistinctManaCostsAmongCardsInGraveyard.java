package com.github.laxika.magicalvibes.model.amount;

/**
 * The number of distinct printed mana costs among nonland, non-token cards in the scoped
 * graveyard(s). Cards without a mana cost are not counted.
 */
public record DistinctManaCostsAmongCardsInGraveyard(CountScope scope) implements DynamicAmount {

    public DistinctManaCostsAmongCardsInGraveyard() {
        this(CountScope.CONTROLLER);
    }
}
