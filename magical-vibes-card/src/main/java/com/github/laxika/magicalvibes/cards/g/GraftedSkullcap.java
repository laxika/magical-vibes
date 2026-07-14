package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "7ED", collectorNumber = "298")
public class GraftedSkullcap extends Card {

    public GraftedSkullcap() {
        // At the beginning of your draw step, draw an additional card.
        addEffect(EffectSlot.DRAW_TRIGGERED, new DrawCardEffect(1));

        // At the beginning of your end step, discard your hand.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new DiscardHandEffect());
    }
}
