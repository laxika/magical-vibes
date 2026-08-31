package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller returns up to the evaluated {@code maxCount} cards matching {@code filter} from
 * their own graveyard to the battlefield. If the controller has fewer matching cards than the
 * limit, all are returned automatically; otherwise they choose which ones. Non-mandatory effects
 * may stop before reaching the limit.
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
 * When {@code mandatory} is true and more matching cards exist than the evaluated limit, the
 * controller must choose the full limit rather than declining an individual pick.
 */
public record ReturnCardsFromControllerGraveyardToBattlefieldEffect(
        CardPredicate filter,
        DynamicAmount maxCount,
        boolean manaValueEqualsX,
        Integer maxTotalManaValue,
        boolean mandatory
) implements CardEffect {

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, int maxCount) {
        this(filter, fixed(maxCount), false, null, false);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, int maxCount,
                                                                  int maxTotalManaValue) {
        this(filter, fixed(maxCount), false, maxTotalManaValue, false);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, int maxCount,
                                                                  boolean manaValueEqualsX) {
        this(filter, fixed(maxCount), manaValueEqualsX, null, false);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, DynamicAmount maxCount) {
        this(filter, maxCount, false, null, false);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, DynamicAmount maxCount,
                                                                  boolean mandatory) {
        this(filter, maxCount, false, null, mandatory);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect {
        if (maxTotalManaValue != null && maxTotalManaValue < 0) {
            throw new IllegalArgumentException("maxTotalManaValue cannot be negative");
        }
    }

    private static DynamicAmount fixed(int maxCount) {
        if (maxCount < 0) {
            throw new IllegalArgumentException("maxCount cannot be negative");
        }
        return new Fixed(maxCount);
    }
}
