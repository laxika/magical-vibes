package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessReturnOwnPermanentTypeToHandEffect;

@CardRegistration(set = "M15", collectorNumber = "76")
public class Quickling extends Card {

    public Quickling() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificeUnlessReturnOwnPermanentTypeToHandEffect(CardType.CREATURE, true));
    }
}
