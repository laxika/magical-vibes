package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "274")
@CardRegistration(set = "INR", collectorNumber = "447")
@CardRegistration(set = "SOI", collectorNumber = "269")
public class WildFieldScarecrow extends Card {

    public WildFieldScarecrow() {
        // {2}, Sacrifice this creature: Search your library for up to two basic land cards,
        // reveal them, put them into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(false, "{2}",
                List.of(new SacrificeSelfCost(),
                        new SearchLibraryEffect(new Fixed(2), CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.HAND)),
                "{2}, Sacrifice this creature: Search your library for up to two basic land "
                        + "cards, reveal them, put them into your hand, then shuffle."));
    }
}
