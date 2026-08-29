package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToDealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KLD", collectorNumber = "117")
public class HarnessedLightning extends Card {

    public HarnessedLightning() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new EnergyCountersEffect(3))
                .addEffect(EffectSlot.SPELL, new PayAnyAmountOfEnergyToDealDamageToTargetCreatureEffect());
    }
}
