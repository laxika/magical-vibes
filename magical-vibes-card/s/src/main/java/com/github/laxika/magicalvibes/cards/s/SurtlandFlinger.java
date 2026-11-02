package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "377")
public class SurtlandFlinger extends Card {

    public SurtlandFlinger() {
        addTrigger(Triggers.attacks(),
                new MayEffect(
                        new SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(true),
                        "Sacrifice another creature?"));
    }
}
