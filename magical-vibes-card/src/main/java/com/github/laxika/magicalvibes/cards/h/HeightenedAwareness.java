package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "PCY", collectorNumber = "37")
public class HeightenedAwareness extends Card {

    public HeightenedAwareness() {
        // As this enchantment enters, discard your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DiscardHandEffect());

        // At the beginning of your draw step, draw an additional card.
        addEffect(EffectSlot.DRAW_TRIGGERED, new DrawCardEffect(1));
    }
}
