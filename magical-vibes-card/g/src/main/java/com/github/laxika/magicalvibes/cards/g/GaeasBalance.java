package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForOneCardOfEachSubtypeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "77")
public class GaeasBalance extends Card {

    public GaeasBalance() {
        addCastingCost(new SacrificeMultiplePermanentsCost(5, new PermanentIsLandPredicate()));
        // One search for a land card of each basic land type; the found lands enter together.
        addSpellEffect(new SearchLibraryForOneCardOfEachSubtypeEffect(
                List.of(CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
                        CardSubtype.MOUNTAIN, CardSubtype.FOREST),
                CardType.LAND,
                LibrarySearchDestination.BATTLEFIELD));
    }
}
