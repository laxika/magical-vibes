package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "CHK", collectorNumber = "236")
public class OrochiSustainer extends Card {

    public OrochiSustainer() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
    }
}
