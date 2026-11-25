package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AER", collectorNumber = "65")
public class IroncladRevolutionary extends Card {

    public IroncladRevolutionary() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                SequenceEffect.of(
                        new SacrificePermanentsEffect(1, new PermanentIsArtifactPredicate(), Subjects.you()),
                        new ConditionalEffect(HappenedThisWay.sacrificed(), SequenceEffect.of(
                                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2),
                                new LoseLifeEffect(2, Subjects.eachOpponent())))),
                "Sacrifice an artifact?"));
    }
}
