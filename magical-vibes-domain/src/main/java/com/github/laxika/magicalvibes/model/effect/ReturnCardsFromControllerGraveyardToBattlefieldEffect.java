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
 */
public record ReturnCardsFromControllerGraveyardToBattlefieldEffect(
        CardPredicate filter,
        int maxCount
) implements CardEffect {
}
