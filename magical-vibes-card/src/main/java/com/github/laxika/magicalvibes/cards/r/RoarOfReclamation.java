package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "5DN", collectorNumber = "14")
public class RoarOfReclamation extends Card {

    public RoarOfReclamation() {
        addEffect(EffectSlot.SPELL, new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                Integer.MAX_VALUE, new CardTypePredicate(CardType.ARTIFACT)));
    }
}
