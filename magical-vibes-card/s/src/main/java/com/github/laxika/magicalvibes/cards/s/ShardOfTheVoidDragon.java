package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "56")
public class ShardOfTheVoidDragon extends Card {

    public ShardOfTheVoidDragon() {
        // Whenever this creature attacks, each opponent sacrifices a nonland permanent of their choice.
        addTrigger(Triggers.attacks(), new SacrificePermanentsEffect(
                1, new PermanentNotPredicate(new PermanentIsLandPredicate()), Subjects.eachOpponent()));

        // Whenever an artifact is put into a graveyard from the battlefield, put two +1/+1 counters on this creature.
        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2));

        // Whenever an artifact is put into exile from the battlefield, put two +1/+1 counters on this creature.
        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), Triggers.ARTIFACT, ObjectRelation.ANOTHER),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
