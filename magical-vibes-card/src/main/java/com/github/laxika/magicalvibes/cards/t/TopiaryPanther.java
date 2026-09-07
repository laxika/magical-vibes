package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "179")
public class TopiaryPanther extends Card {

    public TopiaryPanther() {
        addHandActivatedAbility(new ActivatedAbility(false, "{1}{G}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "Basic landcycling {1}{G} ({1}{G}, Discard this card: Search your library for a basic land card, "
                        + "reveal it, put it into your hand, then shuffle.)"));
    }
}
