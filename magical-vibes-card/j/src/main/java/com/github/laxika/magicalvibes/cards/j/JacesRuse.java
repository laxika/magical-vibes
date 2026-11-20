package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WAR", collectorNumber = "273")
public class JacesRuse extends Card {

    public JacesRuse() {
        target(TargetFilters.creature(), 0, 2)
                .addSpellEffect(ReturnToHandEffect.target());

        addSpellEffect(new MayEffect(
                new SearchLibraryAndOrGraveyardForCardToHandEffect(new CardNamedPredicate("Jace, Arcane Strategist")),
                "Search your library and/or graveyard for a card named Jace, Arcane Strategist?"
        ));
    }
}
