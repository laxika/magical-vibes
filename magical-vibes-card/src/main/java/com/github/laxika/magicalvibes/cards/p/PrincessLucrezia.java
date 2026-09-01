package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "LEG", collectorNumber = "249")
public class PrincessLucrezia extends Card {

    public PrincessLucrezia() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));
    }
}
