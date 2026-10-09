package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "137")
public class DeathPulse extends Card {

    public DeathPulse() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-4, -4));

        addCycling("{1}{B}{B}");
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_SELF_CYCLED,
                new com.github.laxika.magicalvibes.model.effect.MayEffect(
                        new BoostTargetCreatureEffect(-1, -1), "Give target creature -1/-1?"));
    }
}
