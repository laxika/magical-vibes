package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;

@CardRegistration(set = "MRD", collectorNumber = "286")
public class VaultOfWhispers extends Card {

    public VaultOfWhispers() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
