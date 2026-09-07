package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;

@CardRegistration(set = "DIS", collectorNumber = "73")
public class StalkingVengeance extends Card {

    public StalkingVengeance() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new DealDamageToTargetPlayerOrPlaneswalkerEffect(new EventValue()));
    }
}
