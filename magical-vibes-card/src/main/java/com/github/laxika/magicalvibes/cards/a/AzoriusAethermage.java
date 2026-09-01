package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "DIS", collectorNumber = "104")
public class AzoriusAethermage extends Card {

    public AzoriusAethermage() {
        addEffect(EffectSlot.ON_CONTROLLER_PERMANENT_RETURNED_TO_HAND,
                new MayPayManaEffect("{1}", new DrawCardEffect(), "Pay {1} to draw a card?"));
    }
}
