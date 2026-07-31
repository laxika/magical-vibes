package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceToHandAtNextCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "144")
public class ThawingGlaciers extends Card {

    public ThawingGlaciers() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {1}, {T}: Search your library for a basic land card, put that card onto the battlefield
        // tapped, then shuffle. Return this land to its owner's hand at the beginning of the next
        // cleanup step. The delayed bounce is scheduled first so the library search stays last.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(new ReturnSourceToHandAtNextCleanupEffect(),
                        new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED)),
                "{1}, {T}: Search your library for a basic land card, put that card onto the battlefield tapped, then shuffle. Return this land to its owner's hand at the beginning of the next cleanup step."
        ));
    }
}
