package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "126")
public class MischiefAndMayhem extends Card {

    public MischiefAndMayhem() {
        // Up to two target creatures each get +4/+4 until end of turn.
        target(TargetFilters.creature(), 0, 2)
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 4));
    }
}
