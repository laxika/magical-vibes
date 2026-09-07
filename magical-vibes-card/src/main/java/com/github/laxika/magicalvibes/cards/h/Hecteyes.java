package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

@CardRegistration(set = "FIN", collectorNumber = "103")
public class Hecteyes extends Card {

    public Hecteyes() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT));
    }
}
