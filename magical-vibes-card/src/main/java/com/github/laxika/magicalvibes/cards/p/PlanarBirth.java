package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "31")
public class PlanarBirth extends Card {

    public PlanarBirth() {
        addEffect(EffectSlot.SPELL,
                new EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(
                        Integer.MAX_VALUE,
                        new CardAllOfPredicate(List.of(
                                new CardSupertypePredicate(CardSupertype.BASIC),
                                new CardTypePredicate(CardType.LAND))),
                        null,
                        false,
                        true));
    }
}
