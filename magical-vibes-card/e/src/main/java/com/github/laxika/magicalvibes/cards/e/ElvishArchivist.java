package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WOE", collectorNumber = "168")
@CardRegistration(set = "WOE", collectorNumber = "355")
public class ElvishArchivist extends Card {

    public ElvishArchivist() {
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL),
                OncePerTurnTriggerEffect.keyed(new PutCountersOnSourceEffect(1, 1, 2), "artifact"));
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL),
                OncePerTurnTriggerEffect.keyed(new DrawCardEffect(1), "enchantment"));
    }
}
