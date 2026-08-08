package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "DGM", collectorNumber = "90")
public class PilferedPlans extends Card {

    public PilferedPlans() {
        addEffect(EffectSlot.SPELL, new MillEffect(2, MillRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
    }
}
