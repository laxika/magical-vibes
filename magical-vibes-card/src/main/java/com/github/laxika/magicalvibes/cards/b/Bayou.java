package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "SUM", collectorNumber = "283")
@CardRegistration(set = "3ED", collectorNumber = "283")
@CardRegistration(set = "VMA", collectorNumber = "293")
public class Bayou extends Card {

    public Bayou() {
        // {T}: Add {B} or {G}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
