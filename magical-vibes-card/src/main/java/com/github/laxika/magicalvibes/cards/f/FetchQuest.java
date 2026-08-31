package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndPutMilledCardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

public class FetchQuest extends Card {

    public FetchQuest() {
        addEffect(EffectSlot.SPELL, new MillControllerAndPutMilledCardOntoBattlefieldEffect(
                7,
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        new CardTypePredicate(CardType.LAND)))));
    }
}
