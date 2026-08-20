package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.GreatestCreatureTypeCountAmongControlled;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "KHM", collectorNumber = "122")
public class BasaltRavager extends Card {

    public BasaltRavager() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToAnyTargetEffect(new GreatestCreatureTypeCountAmongControlled()));
    }
}
