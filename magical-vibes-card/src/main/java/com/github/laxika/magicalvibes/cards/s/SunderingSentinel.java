package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfReturnAtNextUpkeepWithHasteEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifySourceCardEffect;

@CardRegistration(set = "YSOS", collectorNumber = "29")
public class SunderingSentinel extends Card {

    public SunderingSentinel() {
        setStartingIntensity(2);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToAnyTargetEffect(new SourceIntensity()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(new SourceIntensity()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new IntensifySourceCardEffect(1));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ExileSelfReturnAtNextUpkeepWithHasteEffect());
    }
}
