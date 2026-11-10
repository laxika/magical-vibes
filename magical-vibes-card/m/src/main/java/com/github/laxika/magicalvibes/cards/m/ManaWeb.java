package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TapLandsThatCouldProduceSameManaAsTappedLandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "152")
public class ManaWeb extends Card {

    public ManaWeb() {
        addTrigger(Triggers.opponentTapsLandForMana(),
                new TapLandsThatCouldProduceSameManaAsTappedLandEffect());
    }
}
