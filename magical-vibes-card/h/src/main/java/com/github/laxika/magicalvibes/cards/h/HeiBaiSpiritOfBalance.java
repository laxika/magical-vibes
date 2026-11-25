package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetForEachLeavingSourceCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "225")
public class HeiBaiSpiritOfBalance extends Card {

    public HeiBaiSpiritOfBalance() {
        var anotherCreatureOrArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate()
                )),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        var sacrificeTrigger = new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, anotherCreatureOrArtifact, Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2))),
                "Sacrifice another creature or artifact?");

        addTrigger(Triggers.enters(ObjectRelation.THIS), sacrificeTrigger);
        addTrigger(Triggers.attacks(), sacrificeTrigger);
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                new PutCountersOnTargetForEachLeavingSourceCountersEffect());
    }
}
