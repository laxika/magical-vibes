package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPermanentControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "35")
@CardRegistration(set = "OPCA", collectorNumber = "76")
public class Takenuma extends Card {

    public Takenuma() {
        addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new DrawCardForTriggeringPermanentControllerEffect());
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.chaosEnsues(),
                ReturnToHandEffect.target());
    }
}
