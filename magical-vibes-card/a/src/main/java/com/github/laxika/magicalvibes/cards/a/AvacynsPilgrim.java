package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "170")
@CardRegistration(set = "MM3", collectorNumber = "119")
@CardRegistration(set = "SIS", collectorNumber = "48")
@CardRegistration(set = "MIC", collectorNumber = "132")
@CardRegistration(set = "SCD", collectorNumber = "171")
public class AvacynsPilgrim extends Card {

    public AvacynsPilgrim() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.WHITE));
    }
}
