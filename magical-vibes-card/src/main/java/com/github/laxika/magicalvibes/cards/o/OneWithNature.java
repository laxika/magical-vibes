package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SCG", collectorNumber = "125")
public class OneWithNature extends Card {

    public OneWithNature() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new MayEffect(
                                new SearchLibraryEffect(CardPredicateUtils.basicLand(),
                                        LibrarySearchDestination.BATTLEFIELD_TAPPED),
                                "Search your library for a basic land card?"));
    }
}
