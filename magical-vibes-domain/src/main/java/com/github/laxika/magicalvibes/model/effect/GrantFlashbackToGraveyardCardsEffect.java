package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.Set;

/**
 * Grants flashback to matching cards in the controller's graveyard.
 * In a spell slot the grant lasts until end of turn; in a static slot it lasts while
 * the source permanent remains on the battlefield. A {@code null} flashback cost means the
 * card's mana cost.
 */
public record GrantFlashbackToGraveyardCardsEffect(CardPredicate filter, String flashbackCost)
        implements CardEffect {

    public GrantFlashbackToGraveyardCardsEffect(Set<CardType> cardTypes) {
        this(new CardAnyOfPredicate(cardTypes.stream()
                .map(type -> (CardPredicate) new CardTypePredicate(type))
                .toList()), null);
    }

    public GrantFlashbackToGraveyardCardsEffect(CardPredicate filter) {
        this(filter, null);
    }
}
