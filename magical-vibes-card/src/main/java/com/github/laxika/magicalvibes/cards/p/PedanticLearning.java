package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "ODY", collectorNumber = "90")
public class PedanticLearning extends Card {

    public PedanticLearning() {
        addEffect(EffectSlot.ON_ALLY_LAND_CARD_MILLED,
                new MayPayManaEffect("{1}", new DrawCardEffect(1), "Pay {1} to draw a card?"));
    }
}
