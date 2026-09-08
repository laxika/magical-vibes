package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "KHM", collectorNumber = "76")
public class SawItComing extends Card {

    public SawItComing() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addCastingOption(new ForetellCast("{1}{U}"));
    }
}
