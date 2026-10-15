package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;

@CardRegistration(set = "YONE", collectorNumber = "26")
public class QuicksilverLapidary extends Card {

    public QuicksilverLapidary() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureCardToHandEffect("Mox Opal"));
    }
}
