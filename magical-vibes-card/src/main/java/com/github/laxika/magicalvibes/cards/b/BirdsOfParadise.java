package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;


@CardRegistration(set = "6ED", collectorNumber = "217")
@CardRegistration(set = "7ED", collectorNumber = "231")
@CardRegistration(set = "8ED", collectorNumber = "233")
@CardRegistration(set = "10E", collectorNumber = "252")
@CardRegistration(set = "M10", collectorNumber = "168")
@CardRegistration(set = "M11", collectorNumber = "165")
@CardRegistration(set = "M12", collectorNumber = "165")
@CardRegistration(set = "5ED", collectorNumber = "280")
@CardRegistration(set = "4ED", collectorNumber = "234")
@CardRegistration(set = "SUM", collectorNumber = "187")
public class BirdsOfParadise extends Card {

    public BirdsOfParadise() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
