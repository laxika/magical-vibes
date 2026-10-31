package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "MH1", collectorNumber = "170")
@CardRegistration(set = "GN3", collectorNumber = "102")
@CardRegistration(set = "KHC", collectorNumber = "66")
@CardRegistration(set = "FDC", collectorNumber = "214")
public class LlanowarTribe extends Card {

    public LlanowarTribe() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN, 3));
    }
}
