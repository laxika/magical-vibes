package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoGraveyardThenDoubleAndShuffleEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YONE", collectorNumber = "25")
public class MephidrossSlime extends Card {

    public MephidrossSlime() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new ConjureCardIntoGraveyardThenDoubleAndShuffleEffect(MephidrossSlime::new));
    }
}
