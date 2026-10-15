package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "MH2", collectorNumber = "232")
@CardRegistration(set = "BLC", collectorNumber = "281")
@CardRegistration(set = "DRC", collectorNumber = "133")
@CardRegistration(set = "FDC", collectorNumber = "277")
public class OrnithopterOfParadise extends Card {

    public OrnithopterOfParadise() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
