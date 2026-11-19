package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "101")
public class EtchedFamiliar extends Card {

    public EtchedFamiliar() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new LoseLifeEffect(2, Subjects.eachOpponent()),
                new GainLifeEffect(2)));
    }
}
