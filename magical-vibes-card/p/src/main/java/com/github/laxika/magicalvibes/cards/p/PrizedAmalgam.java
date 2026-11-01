package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOI", collectorNumber = "249")
@CardRegistration(set = "2X2", collectorNumber = "264")
@CardRegistration(set = "TSR", collectorNumber = "382")
@CardRegistration(set = "SIR", collectorNumber = "240")
public class PrizedAmalgam extends Card {

    public PrizedAmalgam() {
        // When a creature enters from this graveyard or is cast from it, return this card tapped at
        // the beginning of the next end step.
        addTrigger(new TriggerCondition(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new RegisterDelayedSelfReturnFromGraveyardEffect(null, 0, false, true));
    }
}
