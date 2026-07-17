package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GatherSpecimensEffect;

@CardRegistration(set = "ALA", collectorNumber = "45")
public class GatherSpecimens extends Card {

    public GatherSpecimens() {
        // If a creature would enter the battlefield under an opponent's control this turn, it enters
        // under your control instead.
        addEffect(EffectSlot.SPELL, new GatherSpecimensEffect());
    }
}
