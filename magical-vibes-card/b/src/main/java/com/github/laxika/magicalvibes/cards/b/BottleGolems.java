package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDyingSourcePowerEffect;

@CardRegistration(set = "MH2", collectorNumber = "222")
public class BottleGolems extends Card {

    public BottleGolems() {
        addEffect(EffectSlot.ON_DEATH, new GainLifeEqualToDyingSourcePowerEffect());
    }
}
