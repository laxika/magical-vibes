package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateBoonEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchAllCreaturesPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;

@CardRegistration(set = "YDSK", collectorNumber = "24")
public class MangledSoulrager extends Card {

    public MangledSoulrager() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SwitchAllCreaturesPowerToughnessEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateBoonEffect(12, new SwitchPowerToughnessEffect()));
        addCycling("{1}{U}");
    }
}
