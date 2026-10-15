package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M21", collectorNumber = "329")
public class LilianasScorn extends Card {

    public LilianasScorn() {
        // Destroy target creature.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());

        // You may search your library and/or graveyard for a card named Liliana, Death Mage,
        // reveal it, and put it into your hand. If you search your library this way, shuffle.
        addEffect(EffectSlot.SPELL, new MayEffect(
                new SearchLibraryAndOrGraveyardForNamedCardToHandEffect("Liliana, Death Mage"),
                "Search your library and/or graveyard for a card named Liliana, Death Mage?"
        ));
    }
}
