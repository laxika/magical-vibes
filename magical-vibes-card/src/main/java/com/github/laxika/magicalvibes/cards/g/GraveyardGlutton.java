package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class GraveyardGlutton extends Card {

    public GraveyardGlutton() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, exileFromGraveyard());
        addEffect(EffectSlot.ON_ATTACK, exileFromGraveyard());
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL, new CounterUnlessDiscardsEffect());
    }

    private ExileCardsFromGraveyardEffect exileFromGraveyard() {
        return new ExileCardsFromGraveyardEffect(
                2,
                new CardTypePredicate(CardType.CREATURE),
                1,
                1,
                false,
                true
        );
    }
}
