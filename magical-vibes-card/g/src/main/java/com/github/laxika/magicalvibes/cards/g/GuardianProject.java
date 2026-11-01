package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfEnteringCreatureHasUniqueNameEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "130")
@CardRegistration(set = "SLD", collectorNumber = "2172")
@CardRegistration(set = "RVR", collectorNumber = "146")
@CardRegistration(set = "PIP", collectorNumber = "199")
@CardRegistration(set = "PIP", collectorNumber = "469")
@CardRegistration(set = "PIP", collectorNumber = "727")
@CardRegistration(set = "PIP", collectorNumber = "997")
public class GuardianProject extends Card {

    public GuardianProject() {
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new DrawCardIfEnteringCreatureHasUniqueNameEffect());
    }
}
