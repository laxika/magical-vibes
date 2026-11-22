package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Objects;

/**
 * "The exiled card perpetually becomes an enchantment. (It loses all other card types.)": each card {@code cards}
 * names that is in exile has only {@code cardType} from then on, so none of the other types' subtypes.
 *
 * @param cards    which cards
 * @param cardType their one card type
 */
public record PerpetuallyBecomeOnlyCardTypeEffect(CardSubject cards, CardType cardType) implements CardEffect {

    public PerpetuallyBecomeOnlyCardTypeEffect {
        Objects.requireNonNull(cardType, "cardType");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names the cards an earlier step moved: " + cards);
        }
    }
}
