package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "247")
public class TimeOfNeed extends Card {

    public TimeOfNeed() {
        // Search your library for a legendary creature card, reveal it, put it into your hand,
        // then shuffle.
        addEffect(EffectSlot.SPELL,
                new SearchLibraryEffect(new CardAllOfPredicate(List.of(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                        new CardTypePredicate(CardType.CREATURE)))));
    }
}
