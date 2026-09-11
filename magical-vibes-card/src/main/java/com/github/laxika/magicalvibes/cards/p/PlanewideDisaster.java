package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "OPC2", collectorNumber = "5")
public class PlanewideDisaster extends Card {

    public PlanewideDisaster() {
        addEffect(EffectSlot.ENCOUNTER_TRIGGERED,
                new DestroyAllPermanentsEffect(new PermanentIsCreaturePredicate()));
    }
}
