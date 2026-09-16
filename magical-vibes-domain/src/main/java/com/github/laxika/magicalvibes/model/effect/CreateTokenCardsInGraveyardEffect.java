package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/** Creates persistent token cards in the controller's graveyard. */
public record CreateTokenCardsInGraveyardEffect(
        int amount,
        CreateTokenEffect tokenTemplate,
        String manaCost,
        String cardText,
        List<ActivatedAbility> graveyardActivatedAbilities
) implements TokenCreatingEffect {

    public CreateTokenCardsInGraveyardEffect {
        if (amount < 0) {
            throw new IllegalArgumentException("Token-card amount cannot be negative");
        }
        if (tokenTemplate == null) {
            throw new IllegalArgumentException("Token-card effect requires a token template");
        }
        if (manaCost == null) {
            throw new IllegalArgumentException("Token-card effect requires a mana cost");
        }
        if (cardText == null) {
            throw new IllegalArgumentException("Token-card effect requires card text");
        }
        if (graveyardActivatedAbilities == null) {
            throw new IllegalArgumentException("Token-card effect requires a graveyard ability list");
        }
        graveyardActivatedAbilities = List.copyOf(graveyardActivatedAbilities);
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new Fixed(amount);
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
