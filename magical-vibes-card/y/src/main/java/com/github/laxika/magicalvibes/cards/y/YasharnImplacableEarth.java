package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.PlayersCantPayLifeOrSacrificeNonlandPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForOneCardOfEachSubtypeEffect;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "240")
public class YasharnImplacableEarth extends Card {

    public YasharnImplacableEarth() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SearchLibraryForOneCardOfEachSubtypeEffect(
                List.of(CardSubtype.FOREST, CardSubtype.PLAINS), null, LibrarySearchDestination.HAND, true));
        addStaticEffect(new PlayersCantPayLifeOrSacrificeNonlandPermanentsEffect());
    }
}
