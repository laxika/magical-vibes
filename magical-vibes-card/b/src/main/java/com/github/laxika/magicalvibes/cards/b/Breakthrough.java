package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TOR", collectorNumber = "26")
@CardRegistration(set = "2X2", collectorNumber = "41")
public class Breakthrough extends Card {

    public Breakthrough() {
        addSpellEffect(new DrawCardEffect(4));
        addSpellEffect(new DiscardEffect(
                new Max(new Fixed(0), new Sum(
                        new CardsInHand(CountScope.CONTROLLER),
                        new Scaled(new XValue(), -1))),
                DiscardRecipient.CONTROLLER));
    }
}
