package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLE", collectorNumber = "92")
public class DutifulKnowledgeSeeker extends Card {

    public DutifulKnowledgeSeeker() {
        addTrigger(new TriggerCondition(Triggers.putIntoLibrary(Triggers.NONTOKEN, ObjectRelation.ANOTHER)),
                TriggerOptions.ONE_OR_MORE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect(
                        PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect.Destination.BOTTOM)),
                "{3}: Put target card from a graveyard on the bottom of its owner's library."
        ));
    }
}
