package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPutMatchingOntoBattlefieldWithCounterAndTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "31")
public class ArbiterOfTheIdeal extends Card {

    public ArbiterOfTheIdeal() {
        addEffect(EffectSlot.ON_SELF_BECOMES_UNTAPPED,
                new RevealTopCardMayPutMatchingOntoBattlefieldWithCounterAndTypeEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.ARTIFACT),
                                new CardTypePredicate(CardType.CREATURE),
                                new CardTypePredicate(CardType.LAND)
                        )),
                        CounterType.MANIFESTATION,
                        CardType.ENCHANTMENT
                ));
    }
}
