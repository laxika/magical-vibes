package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "GS1", collectorNumber = "36")
public class JourneyForTheElixir extends Card {

    public JourneyForTheElixir() {
        // Search your library and graveyard for a basic land card and a card named Jiang Yanggu,
        // reveal them, put them into your hand, then shuffle.
        // The two searches are one instruction, so the library is shuffled only after the second (CR 701.23h).
        addSpellEffect(new SearchLibraryAndOrGraveyardForCardToHandEffect(
                CardPredicateUtils.basicLand(), false));
        addSpellEffect(new SearchLibraryAndOrGraveyardForCardToHandEffect(
                new CardNamedPredicate("Jiang Yanggu"), true));
    }
}
