package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsChosenPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMID", collectorNumber = "30")
public class PredatorySludge extends Card {

    public PredatorySludge() {
        addStaticEffect(new ChoosePermanentOnEnterEffect(
                Subjects.controlledBy(
                Subjects.eachOpponent(), new PermanentTruePredicate()), "a permanent you don't control"));
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, new PermanentIsChosenPermanentPredicate(),
                        ObjectRelation.ANY),
                new ConjureCardNamedIntoHandEffect("YMID", "30", false));
    }
}
