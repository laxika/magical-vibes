package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

@CardRegistration(set = "GTC", collectorNumber = "132")
public class SereneRemembrance extends Card {

    public SereneRemembrance() {
        // Shuffle Serene Remembrance and up to three target cards from a single graveyard into their
        // owners' libraries.
        target(new GraveyardCardPredicateTargetFilter(null, GraveyardSearchScope.ALL_GRAVEYARDS), 0, 3)
                .addSpellEffect(new ShuffleTargetCardsFromGraveyardIntoOwnersLibrariesEffect(null, 3, true));
        addSpellEffect(new ShuffleIntoLibraryEffect());
    }
}
