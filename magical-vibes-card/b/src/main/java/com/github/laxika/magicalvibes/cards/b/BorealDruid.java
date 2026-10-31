package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "CSP", collectorNumber = "105")
public class BorealDruid extends Card {

    public BorealDruid() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
