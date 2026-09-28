package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "HBG", collectorNumber = "254")
public class BronzeWalrus extends Card {

    public BronzeWalrus() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ScryEffect(2));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
