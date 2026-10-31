package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "M20", collectorNumber = "344")
@CardRegistration(set = "ANB", collectorNumber = "109")
public class WoodlandMystic extends Card {

    public WoodlandMystic() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
