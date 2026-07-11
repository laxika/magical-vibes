package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "SOS", collectorNumber = "65")
public class QuickStudy extends Card {

    public QuickStudy() {
        // Draw two cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
