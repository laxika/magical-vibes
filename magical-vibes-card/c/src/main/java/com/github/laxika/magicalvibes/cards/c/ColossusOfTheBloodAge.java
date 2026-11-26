package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "181")
public class ColossusOfTheBloodAge extends Card {

    public ColossusOfTheBloodAge() {
        // When this creature enters, it deals 3 damage to each opponent and you gain 3 life.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                DealDamageEffect.to(Subjects.eachOpponent(), 3),
                new GainLifeEffect(3));

        // When this creature dies, discard any number of cards, then draw that many cards plus one.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DiscardUpToThenDrawThatManyEffect(
                DiscardUpToThenDrawThatManyEffect.ANY_NUMBER, 1));
    }
}
