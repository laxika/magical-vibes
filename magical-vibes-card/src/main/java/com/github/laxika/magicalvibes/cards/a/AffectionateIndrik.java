package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FDN", collectorNumber = "211")
@CardRegistration(set = "GRN", collectorNumber = "121")
public class AffectionateIndrik extends Card {

    public AffectionateIndrik() {
        // When this creature enters, you may have it fight target creature you don't control.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                        new EnteringCreatureFightsTargetCreatureEffect(),
                        "Have it fight target creature you don't control?"
                ));
    }
}
