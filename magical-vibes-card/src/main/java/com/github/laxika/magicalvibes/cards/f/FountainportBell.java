package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "245")
public class FountainportBell extends Card {

    public FountainportBell() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(
                        new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.TOP_OF_LIBRARY),
                        "Search your library for a basic land card?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{1}, Sacrifice this artifact: Draw a card."
        ));
    }
}
