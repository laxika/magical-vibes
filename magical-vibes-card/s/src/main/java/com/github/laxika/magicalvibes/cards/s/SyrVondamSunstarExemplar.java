package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "EOE", collectorNumber = "231")
public class SyrVondamSunstarExemplar extends Card {

    public SyrVondamSunstarExemplar() {
        CardEffect counterAndLife = SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new GainLifeEffect(1));
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), counterAndLife);
        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD),
                Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL), counterAndLife);

        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addTrigger(Triggers.dies(new PermanentPowerAtLeastPredicate(4), ObjectRelation.THIS),
                        new DestroyTargetPermanentEffect())
                .addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), new PermanentPowerAtLeastPredicate(4),
                                ObjectRelation.THIS),
                        new DestroyTargetPermanentEffect());
    }
}
