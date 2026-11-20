package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZNR", collectorNumber = "137")
public class CleansingWildfire extends Card {

    public CleansingWildfire() {
        target(TargetFilters.land())
                .addSpellEffect(new DestroyTargetPermanentEffect()).addSpellEffect(new MayEffect(new SearchLibraryEffect(
                                CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED).searchedBy(Subjects.controllerOf(Subjects.target())),
                                "You may search your library for a basic land card.", null, MayChoicePlayer.TARGET_PERMANENT_CONTROLLER))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
