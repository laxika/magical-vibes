package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsWithTargetCreatureNameEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NEM", collectorNumber = "109")
public class PackHunt extends Card {

    public PackHunt() {
        // Search your library for up to three cards with the same name as target creature,
        // reveal them, put them into your hand, then shuffle.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new SearchLibraryForCardsWithTargetCreatureNameEffect(3));
    }
}
