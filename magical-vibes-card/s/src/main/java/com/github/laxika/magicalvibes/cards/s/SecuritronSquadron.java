package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "23")
@CardRegistration(set = "PIP", collectorNumber = "328")
@CardRegistration(set = "PIP", collectorNumber = "551")
@CardRegistration(set = "PIP", collectorNumber = "856")
public class SecuritronSquadron extends Card {

    public SecuritronSquadron() {
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{3}")));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenCopyOfSourceEffect(false, new RepeatedAdditionalCostCount("{3}")));
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentIsTokenPredicate())),
                        ObjectRelation.YOU_CONTROL),
                PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
