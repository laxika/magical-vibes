package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "IMA", collectorNumber = "227")
public class SandstoneOracle extends Card {

    public SandstoneOracle() {
        // When this creature enters, draw cards equal to the difference between the opponent's
        // hand size and yours. A non-positive difference draws nothing.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DrawCardEffect(new Sum(
                        new CardsInHand(CountScope.OPPONENTS),
                        new Scaled(new CardsInHand(CountScope.CONTROLLER), -1))));
    }
}
