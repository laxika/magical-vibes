package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "APC", collectorNumber = "109")
public class LlanowarDead extends Card {

    public LlanowarDead() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
