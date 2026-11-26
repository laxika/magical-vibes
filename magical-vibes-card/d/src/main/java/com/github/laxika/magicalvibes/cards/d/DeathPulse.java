package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "137")
public class DeathPulse extends Card {

    public DeathPulse() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-4, -4));

        addCycling("{1}{B}{B}");
        target(TargetFilters.creature()).addTrigger(Triggers.youCycleThisCard(),
                new com.github.laxika.magicalvibes.model.effect.MayEffect(
                        BoostEffect.target(-1, -1), "Give target creature -1/-1?"));
    }
}
