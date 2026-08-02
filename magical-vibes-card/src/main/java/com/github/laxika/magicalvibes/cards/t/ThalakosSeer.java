package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TMP", collectorNumber = "94")
public class ThalakosSeer extends Card {

    public ThalakosSeer() {
        // "When this creature leaves the battlefield, draw a card."
        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD, new DrawCardEffect(1));
    }
}
