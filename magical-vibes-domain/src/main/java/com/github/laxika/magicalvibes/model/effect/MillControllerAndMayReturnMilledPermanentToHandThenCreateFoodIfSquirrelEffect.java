package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Mills cards, offers one milled permanent card for return to hand, and creates a Food token if
 * the controller controls a Squirrel or accepts a Squirrel card.
 */
public record MillControllerAndMayReturnMilledPermanentToHandThenCreateFoodIfSquirrelEffect(
        int count, CreateTokenEffect foodEffect) implements TokenCreatingEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return foodEffect.amount();
    }

    @Override
    public CardType tokenType() {
        return foodEffect.tokenType();
    }

    @Override
    public int tokenPower() {
        return foodEffect.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return foodEffect.tokenToughness();
    }
}
