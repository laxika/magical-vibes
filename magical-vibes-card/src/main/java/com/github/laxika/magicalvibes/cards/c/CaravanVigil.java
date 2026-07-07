package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "173")
public class CaravanVigil extends Card {

    public CaravanVigil() {
        // Search your library for a basic land card, reveal it, put it into your hand, then shuffle.
        // Morbid — If a creature died this turn, you may put that card onto the battlefield instead.
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Morbid(), 
                new SearchLibraryEffect(CardPredicateUtils.basicLand()),
                new SearchLibraryEffect(
                        new CardAllOfPredicate(List.of(new CardSupertypePredicate(CardSupertype.BASIC), new CardTypePredicate(CardType.LAND))),
                        LibrarySearchDestination.BATTLEFIELD)
        ));
    }
}
