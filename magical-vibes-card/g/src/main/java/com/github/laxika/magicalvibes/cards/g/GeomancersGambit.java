package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
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
                .addSpellEffect(new DestroyTargetPermanentEffect()).addSpellEffect(new MayEffect(new SearchLibraryEffect(
                                CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD).searchedBy(Subjects.controllerOf(Subjects.target())),
                                "Search your library for a basic land card?", null, MayChoicePlayer.TARGET_PERMANENT_CONTROLLER))
                .addSpellEffect(new DrawCardEffect());
    }
}
