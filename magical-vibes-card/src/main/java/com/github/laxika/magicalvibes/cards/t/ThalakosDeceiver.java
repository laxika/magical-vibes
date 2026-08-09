package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STH", collectorNumber = "45")
public class ThalakosDeceiver extends Card {

    public ThalakosDeceiver() {
        // Whenever this creature attacks and isn't blocked, you may sacrifice it. If you do, gain
        // control of target creature.
        target(TargetFilters.creature());
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(new SacrificeSelfThenEffect(new GainControlOfTargetEffect(ControlDuration.PERMANENT)),
                        "You may sacrifice it. If you do, gain control of target creature."));
    }
}
