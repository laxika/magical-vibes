package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.EventValueAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "595")
public class Pompeii extends Card {

    public Pompeii() {
        PutCountersEffect eruptionCounter = PutCountersEffect.onSelf(CounterType.ERUPTION);
        addTrigger(Triggers.planeswalkToThis(), eruptionCounter);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), eruptionCounter);

        addTrigger(Triggers.youRollOneOrMoreDice(),
                TriggerOptions.DEFAULT.withEventCondition(new NotCondition(new EventValueAtLeast(1))),
                SequenceEffect.of(new ScryEffect(2), eruptionCounter));

        addTrigger(Triggers.chaosEnsues(), SequenceEffect.of(
                DealDamageEffect.toEachCreature(new CountersOnSource(CounterType.ERUPTION))
                        .andTo(Subjects.eachPlayer()),
                new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(), Subjects.eachPlayer()),
                new PlaneswalkEffect()));
    }
}
