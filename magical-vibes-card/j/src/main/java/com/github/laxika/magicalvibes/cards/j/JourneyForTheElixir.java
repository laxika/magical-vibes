package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "GS1", collectorNumber = "36")
public class JourneyForTheElixir extends Card {

    public JourneyForTheElixir() {
        // Search your library and graveyard for a basic land card and a card named Jiang Yanggu,
        // reveal them, put them into your hand, then shuffle.
        // The two searches are one instruction, so the library is shuffled once (CR 701.23h). "Then shuffle" isn't
        // "if you search your library this way": the library is shuffled whatever was found, and even when it
        // couldn't be searched (the Leonin Arbiter and Mindlock Orb rulings).
        addSpellEffect(new SearchLibraryAndOrGraveyardForCardToHandEffect(
                CardPredicateUtils.basicLand(), false));
        addSpellEffect(new SearchLibraryAndOrGraveyardForCardToHandEffect(
                new CardNamedPredicate("Jiang Yanggu"), false));
        addSpellEffect(new ShuffleLibraryEffect(false));
    }
}
