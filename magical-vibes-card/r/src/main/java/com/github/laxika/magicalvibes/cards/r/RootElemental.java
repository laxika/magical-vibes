package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "127")
@CardRegistration(set = "MKC", collectorNumber = "182")
public class RootElemental extends Card {

    public RootElemental() {
        addMorph("{5}{G}{G}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS), new MayEffect(
                new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.CREATURE), "creature"),
                "Put a creature card from your hand onto the battlefield?"));
    }
}
