package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "M19", collectorNumber = "177")
@CardRegistration(set = "AER", collectorNumber = "106")
@CardRegistration(set = "FDN", collectorNumber = "554")
@CardRegistration(set = "KLR", collectorNumber = "159")
public class DruidOfTheCowl extends Card {

    public DruidOfTheCowl() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
