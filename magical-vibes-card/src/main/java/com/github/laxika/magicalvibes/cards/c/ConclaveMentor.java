package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDyingSourcePowerEffect;

@CardRegistration(set = "M21", collectorNumber = "216")
public class ConclaveMentor extends Card {

    public ConclaveMentor() {
        addEffect(EffectSlot.STATIC, new AddOnePlusOneCountersEffect());
        addEffect(EffectSlot.ON_DEATH, new GainLifeEqualToDyingSourcePowerEffect());
    }
}
