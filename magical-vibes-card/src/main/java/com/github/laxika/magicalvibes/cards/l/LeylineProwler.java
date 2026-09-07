package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;

@CardRegistration(set = "WAR", collectorNumber = "202")
public class LeylineProwler extends Card {

    public LeylineProwler() {
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
