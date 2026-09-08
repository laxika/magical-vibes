package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "WWK", collectorNumber = "130")
public class SeersSundial extends Card {

    public SeersSundial() {
        // Landfall — Whenever a land you control enters, you may pay {2}. If you do, draw a card.
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new MayPayManaEffect("{2}", new DrawCardEffect(), "Pay {2} to draw a card?"));
    }
}
