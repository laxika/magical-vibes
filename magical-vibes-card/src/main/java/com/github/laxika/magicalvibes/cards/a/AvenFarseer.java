package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "SCG", collectorNumber = "3")
public class AvenFarseer extends Card {

    public AvenFarseer() {
        addEffect(EffectSlot.ON_SELF_OR_ANY_PERMANENT_TURNS_FACE_UP,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
