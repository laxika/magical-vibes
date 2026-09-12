package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "SUM", collectorNumber = "285")
@CardRegistration(set = "3ED", collectorNumber = "285")
@CardRegistration(set = "ME2", collectorNumber = "235")
@CardRegistration(set = "VMA", collectorNumber = "311")
public class Savannah extends Card {

    public Savannah() {
        // {T}: Add {G} or {W}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
