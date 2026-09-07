package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchOutsideGameForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class Granted extends Card {

    public Granted() {
        addEffect(EffectSlot.SPELL, new SearchOutsideGameForCardToHandEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))
        ));
    }
}
