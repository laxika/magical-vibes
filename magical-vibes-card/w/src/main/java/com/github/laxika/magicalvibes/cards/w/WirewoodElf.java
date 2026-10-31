package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "ONS", collectorNumber = "301")
public class WirewoodElf extends Card {

    public WirewoodElf() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
