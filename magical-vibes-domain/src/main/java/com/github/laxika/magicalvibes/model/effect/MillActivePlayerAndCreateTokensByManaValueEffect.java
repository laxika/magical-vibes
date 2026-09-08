package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;

/**
 * The active player mills one card, then creates tokens equal to that card's mana value.
 *
 * <p>The active player is taken from the each-upkeep trigger's stack entry, while the token
 * blueprint is supplied by the card definition. The handler only creates tokens when the card
 * actually reaches the graveyard.</p>
 */
public record MillActivePlayerAndCreateTokensByManaValueEffect(CreateTokenEffect tokenTemplate)
        implements TokenCreatingEffect {

    public MillActivePlayerAndCreateTokensByManaValueEffect {
        if (tokenTemplate == null) {
            throw new IllegalArgumentException("Token template is required");
        }
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new EventValue();
    }

    @Override
    public CardType tokenType() {
        return tokenTemplate.primaryType();
    }

    @Override
    public int tokenPower() {
        return tokenTemplate.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenTemplate.tokenToughness();
    }
}
