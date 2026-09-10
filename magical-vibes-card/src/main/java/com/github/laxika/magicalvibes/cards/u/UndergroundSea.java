package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "SUM", collectorNumber = "290")
@CardRegistration(set = "3ED", collectorNumber = "290")
@CardRegistration(set = "ME2", collectorNumber = "240")
public class UndergroundSea extends Card {

    public UndergroundSea() {
        // {T}: Add {U} or {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
