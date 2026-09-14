package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesWithPowerGreaterThanTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTP", collectorNumber = "1")
public class FellTheMighty extends Card {

    public FellTheMighty() {
        // Destroy all creatures with power greater than target creature's power.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new DestroyAllCreaturesWithPowerGreaterThanTargetEffect());
    }
}
