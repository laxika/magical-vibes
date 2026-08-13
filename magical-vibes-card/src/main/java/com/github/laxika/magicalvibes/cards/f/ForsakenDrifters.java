package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "BNG", collectorNumber = "72")
public class ForsakenDrifters extends Card {

    public ForsakenDrifters() {
        addEffect(EffectSlot.ON_DEATH, new MillEffect(4, MillRecipient.CONTROLLER));
    }
}
