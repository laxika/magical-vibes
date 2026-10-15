package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "STX", collectorNumber = "80")
public class OriqLoremage extends Card {

    public OriqLoremage() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SearchLibraryAndConditionalEffect(
                        null,
                        LibrarySearchDestination.GRAVEYARD,
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        new PutCountersOnSourceCardEffect(CounterType.PLUS_ONE_PLUS_ONE))),
                "{T}: Search your library for a card, put it into your graveyard, then shuffle. "
                        + "If it's an instant or sorcery card, put a +1/+1 counter on this creature."
        ));
    }
}
