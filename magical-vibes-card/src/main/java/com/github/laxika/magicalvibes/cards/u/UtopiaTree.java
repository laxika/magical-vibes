package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;


@CardRegistration(set = "9ED", collectorNumber = "277")
public class UtopiaTree extends Card {

    public UtopiaTree() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
