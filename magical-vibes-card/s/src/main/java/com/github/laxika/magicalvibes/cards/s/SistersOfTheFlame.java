package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "4ED", collectorNumber = "221")
@CardRegistration(set = "DRK", collectorNumber = "74")
public class SistersOfTheFlame extends Card {

    public SistersOfTheFlame() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));
    }
}
