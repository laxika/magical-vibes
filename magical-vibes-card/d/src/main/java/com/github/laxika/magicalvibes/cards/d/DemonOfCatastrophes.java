package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "M19", collectorNumber = "91")
public class DemonOfCatastrophes extends Card {

    public DemonOfCatastrophes() {
        // As an additional cost to cast this spell, sacrifice a creature.
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
    }
}
