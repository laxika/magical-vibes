package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfControllerPoisonedEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "32")
public class CorruptedResolve extends Card {

    public CorruptedResolve() {
        addEffect(EffectSlot.SPELL, new CounterSpellIfControllerPoisonedEffect());
    }
}
