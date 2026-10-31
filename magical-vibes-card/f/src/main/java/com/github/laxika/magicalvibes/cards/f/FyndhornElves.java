package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "ICE", collectorNumber = "244")
@CardRegistration(set = "DKM", collectorNumber = "26")
@CardRegistration(set = "ME1", collectorNumber = "118")
@CardRegistration(set = "VMA", collectorNumber = "210")
@CardRegistration(set = "V13", collectorNumber = "4")
@CardRegistration(set = "FDC", collectorNumber = "204")
public class FyndhornElves extends Card {

    public FyndhornElves() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
