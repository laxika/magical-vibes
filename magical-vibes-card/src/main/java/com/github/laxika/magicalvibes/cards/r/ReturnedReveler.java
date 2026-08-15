package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "JOU", collectorNumber = "79")
public class ReturnedReveler extends Card {

    public ReturnedReveler() {
        // When this creature dies, each player mills three cards.
        addEffect(EffectSlot.ON_DEATH, new MillEffect(3, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_DEATH, new MillEffect(3, MillRecipient.EACH_OPPONENT));
    }
}
