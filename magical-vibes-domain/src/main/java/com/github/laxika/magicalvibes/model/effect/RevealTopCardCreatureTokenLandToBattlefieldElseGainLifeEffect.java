package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Reveals the top card of the controller's library and branches on its card type: a creature card
 * creates {@code creatureToken}, a land card is put onto the battlefield under the controller's
 * control, and a noncreature nonland card makes the controller gain {@code lifeGain} life. Only the
 * land branch moves the revealed card — in the other two branches it stays on top of the library.
 *
 * <p>Used by Druidic Satchel ({@code {2}, {T}} activated ability, 1/1 green Saproling / 2 life).
 */
public record RevealTopCardCreatureTokenLandToBattlefieldElseGainLifeEffect(
        CreateTokenEffect creatureToken,
        int lifeGain
) implements TokenCreatingEffect, LifeGainEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return creatureToken.tokenAmount();
    }

    @Override
    public CardType tokenType() {
        return creatureToken.tokenType();
    }

    @Override
    public int tokenPower() {
        return creatureToken.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return creatureToken.tokenToughness();
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return new Fixed(lifeGain);
    }
}
