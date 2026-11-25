package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "86")
public class BeetleHeadedMerchants extends Card {

    public BeetleHeadedMerchants() {
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                                new PermanentAnyOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsArtifactPredicate()
                                )),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                        )), Subjects.you()), new ConditionalEffect(HappenedThisWay.sacrificed(), SequenceEffect.of(
                                new DrawCardEffect(1),
                                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)))),
                "Sacrifice another creature or artifact?"));
    }
}
