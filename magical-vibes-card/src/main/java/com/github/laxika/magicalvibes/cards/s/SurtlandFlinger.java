package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect;

@CardRegistration(set = "KHM", collectorNumber = "377")
public class SurtlandFlinger extends Card {

    public SurtlandFlinger() {
        addEffect(EffectSlot.ON_ATTACK,
                new MayEffect(
                        new SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(true),
                        "Sacrifice another creature?"));
    }
}
