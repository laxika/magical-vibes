package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Cost that requires exiling any number of cards from the controller's graveyard with a total
 * value at least the specified amount. The value is normally mana value, but can instead be the
 * number of colored mana symbols in the cards' mana costs.
 */
public record CollectEvidenceCost(int minimumManaValue, boolean optional, boolean targetManaValue,
                                  CardPredicate cardFilter, ManaColor manaSymbolColor,
                                  boolean triggersCollectEvidence) implements CostEffect {

    public CollectEvidenceCost(int minimumManaValue) {
        this(minimumManaValue, false, false, null, null, true);
    }

    public CollectEvidenceCost(int minimumManaValue, boolean optional) {
        this(minimumManaValue, optional, false, null, null, true);
    }

    public CollectEvidenceCost(int minimumManaValue, boolean optional, boolean targetManaValue) {
        this(minimumManaValue, optional, targetManaValue, null, null, true);
    }

    public static CollectEvidenceCost forTargetManaValue() {
        return new CollectEvidenceCost(0, false, true, null, null, true);
    }

    public static CollectEvidenceCost forColorManaSymbols(int minimumSymbols, ManaColor color,
                                                           CardPredicate filter) {
        return new CollectEvidenceCost(minimumSymbols, false, false, filter, color, false);
    }

    public boolean usesTargetManaValue() {
        return targetManaValue;
    }

    public CollectEvidenceCost {
        if (minimumManaValue < 0) {
            throw new IllegalArgumentException("Minimum evidence mana value cannot be negative");
        }
        if (targetManaValue && (cardFilter != null || manaSymbolColor != null)) {
            throw new IllegalArgumentException("Target mana value cannot use a card filter or colored mana symbols");
        }
        if (manaSymbolColor == null && !triggersCollectEvidence) {
            throw new IllegalArgumentException("A non-evidence graveyard threshold must count mana symbols");
        }
    }
}
