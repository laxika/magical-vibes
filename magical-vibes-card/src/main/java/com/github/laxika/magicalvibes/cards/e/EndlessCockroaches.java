package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;

@CardRegistration(set = "POR", collectorNumber = "92")
public class EndlessCockroaches extends Card {

    public EndlessCockroaches() {
        // When this creature dies, return it to its owner's hand.
        addEffect(EffectSlot.ON_DEATH, new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
