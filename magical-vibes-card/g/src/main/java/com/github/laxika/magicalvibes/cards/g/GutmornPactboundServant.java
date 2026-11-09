package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsNonlandCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMID", collectorNumber = "28")
public class GutmornPactboundServant extends Card {

    public GutmornPactboundServant() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new EachPlayerDiscardsNonlandCardEffect());
        addTrigger(List.of(Triggers.youDiscardCard(), Triggers.opponentDiscardsCard()),
                TriggerOptions.DEFAULT.withEventCondition(new ControllerTurn()),
                new ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect());
    }
}
