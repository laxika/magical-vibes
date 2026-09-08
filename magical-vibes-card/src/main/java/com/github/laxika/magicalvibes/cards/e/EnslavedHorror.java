package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerMayReturnCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "134")
public class EnslavedHorror extends Card {

    public EnslavedHorror() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachOtherPlayerMayReturnCardsFromGraveyardToBattlefieldEffect(
                        1, new CardTypePredicate(CardType.CREATURE)));
    }
}
