package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "M15", collectorNumber = "242")
@CardRegistration(set = "DST", collectorNumber = "164")
public class DarksteelCitadel extends Card {

    public DarksteelCitadel() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
