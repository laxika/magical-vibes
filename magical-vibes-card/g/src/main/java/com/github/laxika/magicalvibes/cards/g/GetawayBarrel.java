package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsPutRandomMatchingOntoBattlefieldRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOB", collectorNumber = "98")
public class GetawayBarrel extends Card {

    public GetawayBarrel() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new RevealTopCardsPutRandomMatchingOntoBattlefieldRestOnBottomEffect(
                        13, new CardTypePredicate(CardType.CREATURE)));
    }
}
