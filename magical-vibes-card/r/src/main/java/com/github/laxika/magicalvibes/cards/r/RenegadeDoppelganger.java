package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "84")
public class RenegadeDoppelganger extends Card {

    public RenegadeDoppelganger() {
        // Whenever another creature you control enters, you may have this creature become a copy
        // of that creature until end of turn.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect().ofTriggeringPermanent(),
                        "Have this creature become a copy of that creature until end of turn?"));
    }
}
