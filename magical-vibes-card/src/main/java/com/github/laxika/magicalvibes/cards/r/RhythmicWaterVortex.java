package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GS1", collectorNumber = "18")
public class RhythmicWaterVortex extends Card {

    public RhythmicWaterVortex() {
        // Return up to two target creatures to their owners' hands.
        target(TargetFilters.creature(), 0, 2).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());

        // Search your library and/or graveyard for Mu Yanling, reveal it, and put it into your hand.
        addEffect(EffectSlot.SPELL, new SearchLibraryAndOrGraveyardForCardToHandEffect(
                new CardNamedPredicate("Mu Yanling")));
    }
}
