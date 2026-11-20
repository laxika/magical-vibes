package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "STH", collectorNumber = "45")
public class ThalakosDeceiver extends Card {

    public ThalakosDeceiver() {
        // Whenever this creature attacks and isn't blocked, you may sacrifice it. If you do, gain
        // control of target creature.
        target(TargetFilters.creature());
        addTrigger(Triggers.attacksUnblocked(),
                new MayEffect(SequenceEffect.of(new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), new GainControlOfTargetEffect(Duration.PERMANENT))),
                        "You may sacrifice it. If you do, gain control of target creature."));
    }
}
