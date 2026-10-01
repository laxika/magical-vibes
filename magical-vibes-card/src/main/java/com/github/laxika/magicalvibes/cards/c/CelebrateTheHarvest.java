package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.DistinctPowersAmongControlledCreatures;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "MIC", collectorNumber = "24")
@CardRegistration(set = "MIC", collectorNumber = "62")
public class CelebrateTheHarvest extends Card {

    public CelebrateTheHarvest() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                new DistinctPowersAmongControlledCreatures(),
                CardPredicateUtils.basicLand(),
                LibrarySearchDestination.BATTLEFIELD_TAPPED));
    }
}
