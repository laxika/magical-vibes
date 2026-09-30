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
 * at resolution, subject to their aggregate mana value not exceeding the cap. A dynamic cap is
 * evaluated once as the effect resolves and retained by the choice interaction.
 * When {@code mandatory} is true and more matching cards exist than the evaluated limit, the
 * controller must choose the full limit rather than declining an individual pick.
 * Cards returned by the automatic path enter tapped when {@code enterTapped} is true.
 * When {@code distinctNames} is true, the controller chooses any number of matching cards, with no two chosen cards sharing a
 * name, and all chosen cards enter the battlefield simultaneously.
 */
public record ReturnCardsFromControllerGraveyardToBattlefieldEffect(
        CardPredicate filter,
        DynamicAmount maxCount,
        boolean manaValueEqualsX,
        Integer maxTotalManaValue,
        DynamicAmount dynamicMaxTotalManaValue,
        boolean mandatory,
        boolean enterTapped,
        boolean distinctNames
) implements CardEffect {

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, int maxCount) {
        this(filter, fixed(maxCount), false, null, null, false, false, false);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, int maxCount,
                                                                  int maxTotalManaValue) {
        this(filter, fixed(maxCount), false, maxTotalManaValue, null, false, false, false);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, int maxCount,
                                                                  boolean manaValueEqualsX) {
        this(filter, fixed(maxCount), manaValueEqualsX, null, null, false, false, false);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, DynamicAmount maxCount) {
        this(filter, maxCount, false, null, null, false, false, false);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(CardPredicate filter, DynamicAmount maxCount,
                                                                  boolean mandatory) {
        this(filter, maxCount, false, null, null, mandatory, false, false);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(
            CardPredicate filter, int maxCount, boolean manaValueEqualsX,
            Integer maxTotalManaValue, boolean enterTapped) {
        this(filter, fixed(maxCount), manaValueEqualsX, maxTotalManaValue, null, false, enterTapped, false);
    }

    public ReturnCardsFromControllerGraveyardToBattlefieldEffect(
            CardPredicate filter, DynamicAmount maxCount, boolean manaValueEqualsX,
            Integer maxTotalManaValue, boolean mandatory, boolean enterTapped) {
        this(filter, maxCount, manaValueEqualsX, maxTotalManaValue, null, mandatory, enterTapped, false);
    }

    /** Creates an any-number form capped by a dynamically evaluated total mana value. */
    public static ReturnCardsFromControllerGraveyardToBattlefieldEffect withinTotalManaValue(
            CardPredicate filter, DynamicAmount maxTotalManaValue) {
        return new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                filter, fixed(Integer.MAX_VALUE), false, null, maxTotalManaValue, false, false, false);
    }

    public static ReturnCardsFromControllerGraveyardToBattlefieldEffect anyNumberOfDistinctNames(
            CardPredicate filter) {
        return new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                filter, fixed(Integer.MAX_VALUE), false, null, null, false, false, true);
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
