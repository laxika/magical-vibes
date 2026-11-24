package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsNonlandCardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantAnyColorManaToCastEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "28")
public class GutmornPactboundServant extends Card {

    public GutmornPactboundServant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EachPlayerDiscardsNonlandCardEffect());
        // "They choose another player. That player conjures a duplicate of that card": the discarding player chooses,
        // and the duplicate reads the discarded card as it last existed if it has left its graveyard (CR 608.2h).
        addTrigger(List.of(Triggers.youDiscardCard(), Triggers.opponentDiscardsCard()),
                TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn()),
                new ChoosePlayerEffect(Subjects.except(Subjects.eachPlayer(), Subjects.triggeringPlayer()),
                        "another player").chosenBy(Subjects.triggeringPlayer()),
                ConjureCardsEffect.duplicateOf(Subjects.triggeringCards(), ConjureDestination.intoHand())
                        .conjuredBy(Subjects.playersThisWay(StepKind.CHOOSE)),
                new PerpetuallyGrantAnyColorManaToCastEffect(Subjects.cardsThisWay(StepKind.CONJURE)));
    }
}
