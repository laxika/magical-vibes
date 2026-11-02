package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LeylineSurge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "193")
public class InvasionOfShandalar extends Card {

    public InvasionOfShandalar() {
        setBackFaceCard(new LeylineSurge());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardIsPermanentPredicate(), 3));
    }

    @Override
    public String getBackFaceClassName() {
        return "LeylineSurge";
    }
}
