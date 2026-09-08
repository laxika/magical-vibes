package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "76")
public class DieYoung extends Card {

    public DieYoung() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new EnergyCountersEffect(2))
                .addEffect(EffectSlot.SPELL, new PayAnyAmountOfEnergyToBoostTargetCreatureEffect());
    }
}
