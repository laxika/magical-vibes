package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class GrandmotherRaviSengir extends Card {

    public GrandmotherRaviSengir() {
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS), SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new GainLifeEffect(1)));
    }
}
