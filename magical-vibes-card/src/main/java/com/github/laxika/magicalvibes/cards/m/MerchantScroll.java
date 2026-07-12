package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "8ED", collectorNumber = "91")
public class MerchantScroll extends Card {

    public MerchantScroll() {
        // Search your library for a blue instant card, reveal that card, put it into your hand, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                new CardColorPredicate(CardColor.BLUE),
                new CardTypePredicate(CardType.INSTANT)))));
    }
}
