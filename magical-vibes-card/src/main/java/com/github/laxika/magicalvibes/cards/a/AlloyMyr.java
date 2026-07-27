package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;


@CardRegistration(set = "NPH", collectorNumber = "129")
public class AlloyMyr extends Card {

    public AlloyMyr() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
