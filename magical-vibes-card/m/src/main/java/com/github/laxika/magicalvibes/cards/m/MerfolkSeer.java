package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "MIR", collectorNumber = "76")
public class MerfolkSeer extends Card {

    public MerfolkSeer() {
        // When this creature dies, you may pay {1}{U}. If you do, draw a card.
        addEffect(EffectSlot.ON_DEATH, new MayPayManaEffect("{1}{U}", new DrawCardEffect(1),
                "Pay {1}{U} to draw a card?"));
    }
}
