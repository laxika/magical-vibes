package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "72")
public class RotcrownGhoul extends Card {

    public RotcrownGhoul() {
        // When this creature dies, target player mills five cards.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MillEffect(5, MillRecipient.TARGET_PLAYER));
    }
}
