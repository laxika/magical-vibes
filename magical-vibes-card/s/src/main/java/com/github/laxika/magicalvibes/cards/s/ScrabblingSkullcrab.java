package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "71")
public class ScrabblingSkullcrab extends Card {

    public ScrabblingSkullcrab() {
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MillEffect(2, MillRecipient.TARGET_PLAYER));
        addTrigger(Triggers.youFullyUnlockRoom(),
                new MillEffect(2, MillRecipient.TARGET_PLAYER));
    }
}
