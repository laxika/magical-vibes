package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SCG", collectorNumber = "46")
public class RavenGuildInitiate extends Card {

    public RavenGuildInitiate() {
        addMorph("{2}{U}", new ReturnPermanentsCost(1, new PermanentHasSubtypePredicate(CardSubtype.BIRD)));
    }
}
