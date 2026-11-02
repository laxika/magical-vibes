package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerMayReturnCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "134")
public class EnslavedHorror extends Card {

    public EnslavedHorror() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachOtherPlayerMayReturnCardsFromGraveyardToBattlefieldEffect(
                        1, new CardTypePredicate(CardType.CREATURE)));
    }
}
