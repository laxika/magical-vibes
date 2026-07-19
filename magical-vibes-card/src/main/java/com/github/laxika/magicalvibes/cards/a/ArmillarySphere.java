package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;

@CardRegistration(set = "CON", collectorNumber = "134")
public class ArmillarySphere extends Card {

    public ArmillarySphere() {
        // {2}, {T}, Sacrifice this artifact: Search your library for up to two basic land cards,
        // reveal them, put them into your hand, then shuffle.
        addActivatedAbility(new ActivatedAbility(true, "{2}",
                List.of(new SacrificeSelfCost(),
                        new SearchLibraryEffect(new Fixed(2), CardPredicateUtils.basicLand(),
                                LibrarySearchDestination.HAND)),
                "{2}, {T}, Sacrifice Armillary Sphere: Search your library for up to two basic land "
                        + "cards, reveal them, put them into your hand, then shuffle."));
    }
}
