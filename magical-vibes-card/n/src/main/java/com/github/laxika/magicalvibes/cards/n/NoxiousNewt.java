package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "SOS", collectorNumber = "155")
public class NoxiousNewt extends Card {

    public NoxiousNewt() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
