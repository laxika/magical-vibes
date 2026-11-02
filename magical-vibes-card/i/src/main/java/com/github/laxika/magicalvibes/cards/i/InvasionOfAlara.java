package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AwakenTheMaelstrom;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilTwoNonlandCardsWithManaValueAndCastOneEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "230")
public class InvasionOfAlara extends Card {

    public InvasionOfAlara() {
        setBackFaceCard(new AwakenTheMaelstrom());
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RevealUntilTwoNonlandCardsWithManaValueAndCastOneEffect(4));
    }

    @Override
    public String getBackFaceClassName() {
        return "AwakenTheMaelstrom";
    }
}
