package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Additional cast cost: exile exactly N matching cards from your graveyard or pay a mana cost.
 * The graveyard option is selected with {@code PlayCardRequest.exileGraveyardCardIndices}; a
 * null selection pays the mana option instead.
 */
public record ExileNCardsFromGraveyardOrPayManaCost(int count, CardType requiredType,
                                                    CardPredicate predicate, String manaCost)
        implements CostEffect {

    public ExileNCardsFromGraveyardOrPayManaCost(int count, String manaCost) {
        this(count, null, null, manaCost);
    }

    @Override
    public int consumedGraveyardCardCount() {
        return count;
    }

    @Override
    public CardType consumedGraveyardCardType() {
        return requiredType;
    }

    public CardPredicate consumedGraveyardCardPredicate() {
        return predicate;
    }
}
