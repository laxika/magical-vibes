package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

@CardRegistration(set = "INV", collectorNumber = "204")
public class QuirionSentinel extends Card {

    public QuirionSentinel() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AwardAnyColorManaEffect());
    }
}
