package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;

@CardRegistration(set = "SOI", collectorNumber = "50")
public class BrokenConcentration extends Card {

    public BrokenConcentration() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addCastingOption(new MadnessCast("{3}{U}"));
    }
}
