package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "322")
@CardRegistration(set = "WOE", collectorNumber = "374")
public class WildwoodMentor extends Card {

    public WildwoodMentor() {
        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsAttackingPredicate(),
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                )),
                "Target must be another attacking creature"
        )).addEffect(EffectSlot.ON_ATTACK,
                new BoostTargetCreatureEffect(new SourcePower(), new SourcePower()));
    }
}
