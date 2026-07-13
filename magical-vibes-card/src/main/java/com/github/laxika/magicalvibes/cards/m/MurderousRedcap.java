package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "SHM", collectorNumber = "192")
public class MurderousRedcap extends Card {

    public MurderousRedcap() {
        // Persist is auto-loaded as a keyword from Scryfall; the return mechanic is handled by the engine.
        // When this creature enters, it deals damage equal to its power to any target.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(new SourcePower()));
    }
}
