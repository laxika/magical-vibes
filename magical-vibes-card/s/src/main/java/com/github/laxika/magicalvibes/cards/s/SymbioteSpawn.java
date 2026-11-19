package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPE", collectorNumber = "11")
public class SymbioteSpawn extends Card {

    public SymbioteSpawn() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new LoseLifeEffect(2, Subjects.eachOpponent()),
                new GainLifeEffect(2)));
    }
}
