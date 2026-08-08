package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "32")
public class ChiseiHeartOfOceans extends Card {

    public ChiseiHeartOfOceans() {
        // "At the beginning of your upkeep, sacrifice this creature unless you remove a counter
        // from a permanent you control." Optional: the controller is asked, and declining (or
        // controlling no counters at all) sacrifices Chisei.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ForcedCostOrElseEffect(
                        new RemoveCounterFromControlledPermanentCost(),
                        List.of(new SacrificeSelfEffect()),
                        true));
    }
}
