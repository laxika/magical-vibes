package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "15")
public class GlorifierOfSuffering extends Card {

    public GlorifierOfSuffering() {
        target(TargetFilters.creature(), 0, 2).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(
                        SequenceEffect.of(
                                new SacrificePermanentsEffect(1, new PermanentAllOfPredicate(List.of(
                                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                                        new PermanentAnyOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentIsArtifactPredicate()))
                                )), Subjects.you()),
                                new ConditionalEffect(HappenedThisWay.sacrificed(), new QueueReflexiveAbilityEffect(
                                        new PutCountersEffect(CounterType.PLUS_ONE_PLUS_ONE)))),
                        "Sacrifice another creature or artifact?"));
    }
}
