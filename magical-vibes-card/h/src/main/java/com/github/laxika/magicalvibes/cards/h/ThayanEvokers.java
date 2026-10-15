package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "HBG", collectorNumber = "39")
public class ThayanEvokers extends Card {

    public ThayanEvokers() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConjureCardToHandEffect("M10", "146"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_CONTROLLER_CONJURES,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
