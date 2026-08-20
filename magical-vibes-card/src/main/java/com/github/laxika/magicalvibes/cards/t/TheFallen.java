package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToOpponentsAndPlaneswalkersDamagedBySourceEffect;

@CardRegistration(set = "CHR", collectorNumber = "38")
@CardRegistration(set = "DRK", collectorNumber = "53")
public class TheFallen extends Card {

    public TheFallen() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new DealDamageToOpponentsAndPlaneswalkersDamagedBySourceEffect(1));
    }
}
