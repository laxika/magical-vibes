package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller returns up to {@code maxCount} cards matching {@code filter} from their own
 * graveyard to the battlefield. If the controller has fewer matching cards than {@code maxCount},
 * all are returned automatically; otherwise they choose which ones (declining stops the picks).
 *
 * <p>Example: Reveillark's "return up to two target creature cards with power 2 or less from your
 * graveyard to the battlefield." →
 * {@code new ReturnCardsFromControllerGraveyardToBattlefieldEffect(new CardAllOfPredicate(List.of(
 * new CardTypePredicate(CardType.CREATURE), new CardPowerAtMostPredicate(2))), 2)}
 *
 * <p>When {@code manaValueEqualsX} is set, matching cards are additionally restricted to those
 * whose mana value equals the spell's paid X, and {@code maxCount} is normally
 * {@link Integer#MAX_VALUE} so that every match is returned — Immortal Servitude's "return each
 * creature card with mana value X from your graveyard to the battlefield." When
 * {@code maxTotalManaValue} is set, the controller chooses up to {@code maxCount} matching cards
 * at resolution, subject to their aggregate mana value not exceeding the cap.
 */
public record ReturnCardsFromControllerGraveyardToBattlefieldEffect(
        CardPredicate filter,
        int maxCount,
        boolean manaValueEqualsX,
        Integer maxTotalManaValue
) implements CardEffect {

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, int maxCount) {
        this(filter, maxCount, false, null);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, int maxCount,
                                                                  int maxTotalManaValue) {
        this(filter, maxCount, false, maxTotalManaValue);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, int maxCount,
                                                                  boolean manaValueEqualsX) {
        this(filter, maxCount, manaValueEqualsX, null);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect {
        if (maxCount < 0) {
            throw new IllegalArgumentException("maxCount cannot be negative");
        }
        if (maxTotalManaValue != null && maxTotalManaValue < 0) {
            throw new IllegalArgumentException("maxTotalManaValue cannot be negative");
        }
    }
}
