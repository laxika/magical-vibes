package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.SourceIsOnBattlefield;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "253")
@CardRegistration(set = "DDF", collectorNumber = "55")
@CardRegistration(set = "TSR", collectorNumber = "264")
public class ClockworkHydra extends Card {

    public ClockworkHydra() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(4)));

        var attackOrBlockEffect = ConditionalEffect.unless(
                new AllOf(List.of(new SourceIsOnBattlefield(),
                        new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE))),
                SequenceEffect.of(new RemoveCounterFromSourceEffect(CounterType.PLUS_ONE_PLUS_ONE, 1),
                        new DealDamageToAnyTargetEffect(1)));
        target(new AnyTargetPredicateTargetFilter(
                TargetPredicates.anyTarget().permanentRestriction().orElseThrow(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a creature, planeswalker, battle, or player"))
                .addEffect(EffectSlot.ON_ATTACK, attackOrBlockEffect);
        addEffect(EffectSlot.ON_BLOCK, attackOrBlockEffect);

        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                "{T}: Put a +1/+1 counter on Clockwork Hydra."));
    }
}
