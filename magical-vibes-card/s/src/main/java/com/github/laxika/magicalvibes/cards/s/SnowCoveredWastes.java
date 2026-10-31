package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "MB2", collectorNumber = "121")
public class SnowCoveredWastes extends Card {

    public SnowCoveredWastes() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
