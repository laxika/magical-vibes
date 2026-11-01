package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MID", collectorNumber = "242")
@CardRegistration(set = "DBL", collectorNumber = "242")
public class SlogurkTheOverslime extends Card {

    public SlogurkTheOverslime() {
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.LAND_CARD, ObjectRelation.YOU_OWN),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(3, CounterType.PLUS_ONE_PLUS_ONE),
                        ReturnToHandEffect.self()
                ),
                "Remove three +1/+1 counters from Slogurk: Return it to its owner's hand."
        ));

        addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                ReturnTargetCardsFromGraveyardToHandEffect.forTriggeredAbility(
                        new CardTypePredicate(CardType.LAND), 3));
    }
}
