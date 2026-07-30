package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INR", collectorNumber = "76")
@CardRegistration(set = "AVR", collectorNumber = "67")
public class MistRaven extends Card {

    public MistRaven() {
        // Flying (Scryfall keyword)
        // When this creature enters, return target creature to its owner's hand.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnToHandEffect.target());
    }
}
