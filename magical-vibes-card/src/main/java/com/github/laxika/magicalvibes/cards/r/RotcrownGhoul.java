package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "AVR", collectorNumber = "72")
public class RotcrownGhoul extends Card {

    public RotcrownGhoul() {
        // When this creature dies, target player mills five cards.
        addEffect(EffectSlot.ON_DEATH, new MillEffect(5, MillRecipient.TARGET_PLAYER));
    }
}
