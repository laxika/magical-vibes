package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "SUM", collectorNumber = "289")
@CardRegistration(set = "3ED", collectorNumber = "289")
@CardRegistration(set = "2ED", collectorNumber = "285")
@CardRegistration(set = "ME2", collectorNumber = "239")
@CardRegistration(set = "VMA", collectorNumber = "322")
@CardRegistration(set = "ME4", collectorNumber = "255")
public class Tundra extends Card {

    public Tundra() {
        // {T}: Add {W} or {U}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
