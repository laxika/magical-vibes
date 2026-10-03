package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestOpponentHandSize;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

@CardRegistration(set = "KHC", collectorNumber = "8")
public class TalesOfTheAncestors extends Card {

    public TalesOfTheAncestors() {
        CardsInHand ownHand = new CardsInHand(CountScope.CONTROLLER);
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(new Sum(
                new Max(ownHand, new GreatestOpponentHandSize()),
                new Scaled(ownHand, -1))));
    }
}
