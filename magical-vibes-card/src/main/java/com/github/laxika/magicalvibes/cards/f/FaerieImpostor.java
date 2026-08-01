package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;

@CardRegistration(set = "RTR", collectorNumber = "39")
public class FaerieImpostor extends Card {

    public FaerieImpostor() {
        // When this creature enters, sacrifice it unless you return another creature
        // you control to its owner's hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.CREATURE, true));
    }
}
