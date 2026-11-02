package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandElseGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "43")
public class NeurokFamiliar extends Card {

    public NeurokFamiliar() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RevealTopCardMatchingToHandElseGraveyardEffect(new CardTypePredicate(CardType.ARTIFACT)));
    }
}
