package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MB1", collectorNumber = "125")
@CardRegistration(set = "MH1", collectorNumber = "125")
public class GeomancersGambit extends Card {

    public GeomancersGambit() {
        // Destroy target land. Its controller may search their library for a basic land card,
        // put it onto the battlefield, then shuffle. Draw a card.
        // The controller first chooses whether to search; declining neither searches nor shuffles.
        target(TargetFilters.land())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                        new MayEffect(new SearchLibraryEffect(
                                CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD),
                                "Search your library for a basic land card?"),
                        ThenEffectRecipient.TARGET_CONTROLLER))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
