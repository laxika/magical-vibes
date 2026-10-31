package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TSP", collectorNumber = "239")
@CardRegistration(set = "MB1", collectorNumber = "239")
public class GhostflameSliver extends Card {

    public GhostflameSliver() {
        addStaticEffect(new BecomeColorlessEffect(GrantScope.ALL_PERMANENTS,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }
}
