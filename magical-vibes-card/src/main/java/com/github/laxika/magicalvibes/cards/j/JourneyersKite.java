package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "257")
public class JourneyersKite extends Card {

    public JourneyersKite() {
        // {3}, {T}: Search your library for a basic land card, reveal it, put it into your hand,
        // then shuffle.
        addActivatedAbility(new ActivatedAbility(true, "{3}",
                List.of(new SearchLibraryEffect(CardPredicateUtils.basicLand())),
                "{3}, {T}: Search your library for a basic land card, reveal it, put it into your "
                        + "hand, then shuffle."));
    }
}
