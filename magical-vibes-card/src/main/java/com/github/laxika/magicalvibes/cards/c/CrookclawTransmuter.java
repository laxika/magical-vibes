package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "55")
public class CrookclawTransmuter extends Card {

    public CrookclawTransmuter() {
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SwitchPowerToughnessEffect());
    }
}
