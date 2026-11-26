package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMC", collectorNumber = "22")
@CardRegistration(set = "TMC", collectorNumber = "90")
public class CaseyJonesBackAlleyBrute extends Card {

    public CaseyJonesBackAlleyBrute() {
        target(TargetFilters.attackingCreature())
                .addTrigger(Triggers.attacks(),
                        new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE));

        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addTrigger(Triggers.youPutPlusOneCountersOnCreatureYouControl(),
                        DealDamageEffect.to(Subjects.targetPlayer(), new EventValue()));
    }
}
