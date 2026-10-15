package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MSH", collectorNumber = "149")
public class RedHulk extends Card {

    public RedHulk() {
        target(new AnyTargetPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be another target"
        )).addEffect(EffectSlot.ON_DEALT_DAMAGE,
                SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        DealDamageToAnyTargetEffect.toAnyOtherTarget(
                                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
