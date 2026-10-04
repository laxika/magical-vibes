package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "TSR", collectorNumber = "405")
@CardRegistration(set = "ZNC", collectorNumber = "121")
@CardRegistration(set = "C21", collectorNumber = "280")
@CardRegistration(set = "C20", collectorNumber = "258")
@CardRegistration(set = "MIC", collectorNumber = "166")
@CardRegistration(set = "C18", collectorNumber = "236")
@CardRegistration(set = "C17", collectorNumber = "235")
@CardRegistration(set = "BFZ", collectorNumber = "233")
public class BlightedWoodland extends Card {

    public BlightedWoodland() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        // {3}{G}, {T}, Sacrifice this land: Search your library for up to two basic land cards,
        // put them onto the battlefield tapped, then shuffle.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{G}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(
                                new Fixed(2),
                                CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.BATTLEFIELD_TAPPED)
                ),
                "{3}{G}, {T}, Sacrifice Blighted Woodland: Search your library for up to two basic land "
                        + "cards, put them onto the battlefield tapped, then shuffle."
        ));
    }
}
