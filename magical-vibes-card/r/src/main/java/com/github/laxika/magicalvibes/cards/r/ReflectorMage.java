package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreatureToHandAndOwnerCantCastSameNameUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OGW", collectorNumber = "157")
@CardRegistration(set = "SLD", collectorNumber = "1108")
@CardRegistration(set = "EA2", collectorNumber = "19")
public class ReflectorMage extends Card {

    public ReflectorMage() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ReturnTargetCreatureToHandAndOwnerCantCastSameNameUntilNextTurnEffect());
    }
}
