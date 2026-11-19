package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "233")
public class Shroudstomper extends Card {

    public Shroudstomper() {
        SequenceEffect drain = SequenceEffect.of(
                new LoseLifeEffect(2, Subjects.eachOpponent()),
                new GainLifeEffect(2),
                new DrawCardEffect(1));
        addTrigger(Triggers.enters(ObjectRelation.THIS), drain);
        addTrigger(Triggers.attacks(), drain);
    }
}
