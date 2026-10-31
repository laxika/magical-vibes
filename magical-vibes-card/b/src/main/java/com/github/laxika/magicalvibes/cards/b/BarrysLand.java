package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "MB1", collectorNumber = "113")
public class BarrysLand extends Card {

    public BarrysLand() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
