package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "209")
public class BringToLight extends Card {

    public BringToLight() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY))),
                LibrarySearchDestination.EXILE_FOR_MAY_CAST,
                new ManaValueBound(false, 0)));
    }
}
