package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllArtifactsUpkeepSacrificeUnlessPayEffect;

@CardRegistration(set = "5ED", collectorNumber = "83")
@CardRegistration(set = "4ED", collectorNumber = "68")
public class EnergyFlux extends Card {

    public EnergyFlux() {
        // All artifacts have "At the beginning of your upkeep, sacrifice this artifact unless you pay {2}."
        addEffect(EffectSlot.STATIC, new AllArtifactsUpkeepSacrificeUnlessPayEffect("{2}"));
    }
}
