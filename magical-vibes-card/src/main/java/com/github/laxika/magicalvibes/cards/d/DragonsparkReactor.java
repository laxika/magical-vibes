package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "137")
public class DragonsparkReactor extends Card {

    public DragonsparkReactor() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCountersOnSelfEffect(CounterType.CHARGE));
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new PutCountersOnSelfEffect(CounterType.CHARGE));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(
                        new SacrificeSelfCost(),
                        new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.CHARGE),
                                false, false, 0),
                        new DealDamageToAnyTargetEffect(new CountersOnSource(CounterType.CHARGE),
                                false, false, 1)
                ),
                "{4}, Sacrifice this artifact: It deals damage equal to the number of charge counters on it "
                        + "to target player and that much damage to up to one target creature.",
                List.of(
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.ANY),
                                "Target must be a player"),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(),
                                "Target must be a creature")
                ),
                1,
                2
        ));
    }
}
