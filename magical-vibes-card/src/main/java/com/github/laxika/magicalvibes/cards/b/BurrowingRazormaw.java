package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "BRO", collectorNumber = "173")
public class BurrowingRazormaw extends Card {

    public BurrowingRazormaw() {
        addEffect(EffectSlot.ON_DEATH, new MillEffect(4, MillRecipient.CONTROLLER));
    }
}
