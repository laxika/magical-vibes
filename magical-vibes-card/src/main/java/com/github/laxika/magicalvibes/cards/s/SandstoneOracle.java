package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "C15", collectorNumber = "52")
@CardRegistration(set = "MB1", collectorNumber = "213")
public class SandstoneOracle extends Card {

    public SandstoneOracle() {
        // If the opponent has more cards in hand than you, draw the difference.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DrawCardEffect(new Sum(
                        new CardsInHand(CountScope.OPPONENTS),
                        new Scaled(new CardsInHand(CountScope.CONTROLLER), -1))));
    }
}
