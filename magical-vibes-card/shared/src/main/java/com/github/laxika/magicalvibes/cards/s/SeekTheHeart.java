package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

public class SeekTheHeart extends Card {

    public SeekTheHeart() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSupertypePredicate(CardSupertype.LEGENDARY))
        )));
    }
}
