package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToSourceEffect;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

@CardRegistration(set = "WTH", collectorNumber = "88")
@CardRegistration(set = "7ED", collectorNumber = "172")
@CardRegistration(set = "6ED", collectorNumber = "163")
public class AetherFlash extends Card {

    public AetherFlash() {
        // Whenever a creature enters, this enchantment deals 2 damage to it.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new DealDamageToTriggeringPermanentEffect(2));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withEventCondition(new SourceIsCreature()),
                new DealDamageToSourceEffect(2));
    }
}
