package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STH", collectorNumber = "45")
public class ThalakosDeceiver extends Card {

    public ThalakosDeceiver() {
        // Whenever this creature attacks and isn't blocked, you may sacrifice it. If you do, gain
        // control of target creature.
        target(TargetFilters.creature());
        addTrigger(Triggers.attacksUnblocked(),
                new MayEffect(new SacrificeSelfThenEffect(new GainControlOfTargetEffect(ControlDuration.PERMANENT)),
                        "You may sacrifice it. If you do, gain control of target creature."));
    }
}
