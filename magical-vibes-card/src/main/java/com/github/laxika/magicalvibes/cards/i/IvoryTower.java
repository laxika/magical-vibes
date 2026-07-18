package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "4ED", collectorNumber = "328")
public class IvoryTower extends Card {

    public IvoryTower() {
        // At the beginning of your upkeep, you gain X life, where X is the number of cards in
        // your hand minus 4. X can't be negative (four or fewer cards gains no life), so the
        // amount is floored at 0.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new GainLifeEffect(new Max(
                        new Fixed(0),
                        new Sum(new CardsInHand(CountScope.CONTROLLER), new Fixed(-4)))));
    }
}
