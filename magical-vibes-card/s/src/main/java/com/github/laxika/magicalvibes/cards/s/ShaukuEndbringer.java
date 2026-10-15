package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "142")
public class ShaukuEndbringer extends Card {

    public ShaukuEndbringer() {
        // Shauku can't attack if there's another creature on the battlefield. Expressed as the
        // inverse: it may attack only while at most one creature (itself) is on any battlefield.
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new AnyPlayerControlsPermanentCountAtMost(1, new PermanentIsCreaturePredicate()),
                "no other creature is on the battlefield"
        ));

        // At the beginning of your upkeep, you lose 3 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(3));

        // {T}: Exile target creature and put a +1/+1 counter on Shauku. A single target, so an
        // illegal target on resolution counters the whole ability and no counter is placed.
        addActivatedAbility(new ActivatedAbility(
                true,
                "",
                List.of(
                        new ExileTargetPermanentEffect(),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{T}: Exile target creature and put a +1/+1 counter on Shauku, Endbringer.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature"
                )
        ));
    }
}
