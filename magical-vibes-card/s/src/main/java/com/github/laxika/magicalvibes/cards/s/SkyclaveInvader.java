package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class SkyclaveInvader extends Card {

    public SkyclaveInvader() {
        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS),
                new LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect(
                        new CardTypePredicate(CardType.LAND), false));
    }
}
