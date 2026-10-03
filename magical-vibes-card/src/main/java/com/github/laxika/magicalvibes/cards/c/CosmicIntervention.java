package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileControlledPermanentsInsteadOfDyingThisTurnEffect;

@CardRegistration(set = "KHC", collectorNumber = "3")
public class CosmicIntervention extends Card {

    public CosmicIntervention() {
        addEffect(EffectSlot.SPELL, new ExileControlledPermanentsInsteadOfDyingThisTurnEffect());
    }
}
