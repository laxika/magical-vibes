package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "589")
public class CaptainMarvelShootingStar extends Card {

    public CaptainMarvelShootingStar() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect())
                .addEffect(EffectSlot.ON_ATTACK,
                        new ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect());

        addEffect(EffectSlot.ON_ANY_CREATURE_EXILED_FROM_BATTLEFIELD,
                new GainLifeEffect(new EventValue()));
    }
}
