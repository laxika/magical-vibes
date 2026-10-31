package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "JOU", collectorNumber = "124")
public class GoldenHind extends Card {

    public GoldenHind() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
