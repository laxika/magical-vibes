package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GainLifeWhenOpponentTapsLandOfSubtypeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "42")
@CardRegistration(set = "7ED", collectorNumber = "39")
@CardRegistration(set = "UDS", collectorNumber = "16")
public class Sanctimony extends Card {

    public Sanctimony() {
        // Whenever an opponent taps a Mountain for mana, you may gain 1 life.
        addTrigger(Triggers.playerTapsLandForMana(),
                new GainLifeWhenOpponentTapsLandOfSubtypeEffect(CardSubtype.MOUNTAIN, 1));
    }
}
