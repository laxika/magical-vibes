package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "GRN", collectorNumber = "32")
public class ChemistersInsight extends Card {

    public ChemistersInsight() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addCastingOption(new JumpStartCast());
    }
}
