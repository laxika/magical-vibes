package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "ZEN", collectorNumber = "164")
@CardRegistration(set = "DPA", collectorNumber = "68")
public class GreenweaverDruid extends Card {

    public GreenweaverDruid() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN, 2));
    }
}
