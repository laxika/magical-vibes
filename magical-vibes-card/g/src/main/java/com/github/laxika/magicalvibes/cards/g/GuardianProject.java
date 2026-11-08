package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesNameWithControlledCreatureOrGraveyardCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
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
        // Whenever another nontoken creature you control enters, if it doesn't have the same name as another
        // creature you control or a creature card in your graveyard, draw a card. The intervening "if" is judged as
        // the creature enters and again as the ability resolves, against the creature as it last existed if it has
        // left (CR 603.4, CR 608.2h).
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches(new PermanentNotPredicate(
                        new PermanentSharesNameWithControlledCreatureOrGraveyardCreaturePredicate()))),
                new DrawCardEffect());
    }
}
