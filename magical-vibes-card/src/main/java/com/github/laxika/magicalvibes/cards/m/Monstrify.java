package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "EVE", collectorNumber = "70")
public class Monstrify extends Card {

    public Monstrify() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 4));
        addCastingOption(new Retrace());
    }
}
