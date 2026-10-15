package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ALA", collectorNumber = "28")
public class SoulsGrace extends Card {

    public SoulsGrace() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GainLifeEffect(new TargetPower()));
    }
}
