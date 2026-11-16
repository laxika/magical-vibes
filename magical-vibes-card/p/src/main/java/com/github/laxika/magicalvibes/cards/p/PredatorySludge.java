package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsChosenPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMID", collectorNumber = "30")
public class PredatorySludge extends Card {

    public PredatorySludge() {
        addStaticEffect(new ChooseOpponentPermanentOnEnterEffect());
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, new PermanentIsChosenPermanentPredicate(),
                        ObjectRelation.ANY),
                new ConjureCardNamedIntoHandEffect("YMID", "30", false));
    }
}
