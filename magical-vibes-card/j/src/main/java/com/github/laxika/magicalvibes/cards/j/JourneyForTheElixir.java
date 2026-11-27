package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndGraveyardForCardsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "GS1", collectorNumber = "36")
public class JourneyForTheElixir extends Card {

    public JourneyForTheElixir() {
        // Search your library and graveyard for a basic land card and a card named Jiang Yanggu,
        // reveal them, put them into your hand, then shuffle.
        addSpellEffect(new SearchLibraryAndGraveyardForCardsToHandEffect(List.of(
                CardPredicateUtils.basicLand(), new CardNamedPredicate("Jiang Yanggu"))));
    }
}
