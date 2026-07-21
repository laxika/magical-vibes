package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "111")
public class BeneathTheSands extends Card {

    public BeneathTheSands() {
        // Search your library for a basic land card, put it onto the battlefield tapped, then shuffle.
        addEffect(EffectSlot.SPELL,
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new DrawCardEffect(1)),
                "Cycling {2} ({2}, Discard this card: Draw a card.)"));
    }
}
