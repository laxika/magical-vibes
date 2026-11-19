package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "102")
public class KomasFaithful extends Card {

    public KomasFaithful() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new MillEffect(3, Subjects.you()),
                new MillEffect(3, Subjects.eachOpponent())));
    }
}
