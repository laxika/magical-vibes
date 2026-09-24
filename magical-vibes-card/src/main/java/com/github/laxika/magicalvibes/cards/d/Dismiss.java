package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TMP", collectorNumber = "58")
@CardRegistration(set = "TPR", collectorNumber = "45")
@CardRegistration(set = "C13", collectorNumber = "39")
@CardRegistration(set = "C14", collectorNumber = "106")
public class Dismiss extends Card {

    public Dismiss() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
