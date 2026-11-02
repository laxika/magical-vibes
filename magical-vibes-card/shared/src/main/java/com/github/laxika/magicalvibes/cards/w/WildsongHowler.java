package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class WildsongHowler extends Card {

    public WildsongHowler() {
        LookAtTopCardsEffect lookAtTopCards = LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                6, new CardTypePredicate(CardType.CREATURE));
        addTrigger(Triggers.enters(ObjectRelation.THIS), lookAtTopCards);
        addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS), lookAtTopCards);
    }
}
