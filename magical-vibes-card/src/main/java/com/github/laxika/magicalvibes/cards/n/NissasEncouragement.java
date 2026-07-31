package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndGraveyardForNamedCardsToHandEffect;
import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "203")
public class NissasEncouragement extends Card {

    public NissasEncouragement() {
        // Search your library and graveyard for a card named Forest, a card named Brambleweft
        // Behemoth, and a card named Nissa, Genesis Mage. Reveal those cards, put them into your
        // hand, then shuffle.
        addEffect(EffectSlot.SPELL, new SearchLibraryAndGraveyardForNamedCardsToHandEffect(List.of(
                "Forest",
                "Brambleweft Behemoth",
                "Nissa, Genesis Mage")));
    }
}
