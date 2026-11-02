package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "199")
@CardRegistration(set = "2X2", collectorNumber = "276")
@CardRegistration(set = "CMM", collectorNumber = "355")
@CardRegistration(set = "CMM", collectorNumber = "592")
public class SidisiBroodTyrant extends Card {

    public SidisiBroodTyrant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MillEffect(3, MillRecipient.CONTROLLER));
        addTrigger(Triggers.attacks(), new MillEffect(3, MillRecipient.CONTROLLER));
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN_CREATURE,
                ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                CreateTokenEffect.blackZombie(1));
    }
}
