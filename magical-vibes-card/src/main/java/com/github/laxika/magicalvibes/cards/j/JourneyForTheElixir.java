package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "GS1", collectorNumber = "36")
public class JourneyForTheElixir extends Card {

    public JourneyForTheElixir() {
        // Search your library and graveyard for a basic land card and a card named Jiang Yanggu,
        // reveal them, put them into your hand, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryAndOrGraveyardForCardToHandEffect(
                CardPredicateUtils.basicLand()));
        addEffect(EffectSlot.SPELL, new SearchLibraryAndOrGraveyardForCardToHandEffect(
                new CardNamedPredicate("Jiang Yanggu")));
    }
}
