package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "ALL", collectorNumber = "4")
public class Inheritance extends Card {

    public Inheritance() {
        // Whenever a creature dies, you may pay {3}. If you do, draw a card.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new MayPayManaEffect(
                "{3}", new DrawCardEffect(1), "Pay {3} to draw a card?"
        ));
    }
}
