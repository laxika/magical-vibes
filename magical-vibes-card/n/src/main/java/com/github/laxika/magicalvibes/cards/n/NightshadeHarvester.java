package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSC", collectorNumber = "150")
public class NightshadeHarvester extends Card {

    public NightshadeHarvester() {
        // Whenever a land an opponent controls enters, that player loses 1 life.
        // Put a +1/+1 counter on this creature.
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS), SequenceEffect.of(
                new LoseLifeEffect(1, Subjects.triggeringPlayer()),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
