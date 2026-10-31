package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

@CardRegistration(set = "ULG", collectorNumber = "107")
public class MultaniMaroSorcerer extends Card {

    public MultaniMaroSorcerer() {
        CardsInHand cardsInAllPlayersHands = new CardsInHand(CountScope.ANY_PLAYER);
        addStaticEffect(new SetPowerToughnessToAmountEffect(
                cardsInAllPlayersHands, cardsInAllPlayersHands));
    }
}
