package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "OTJ", collectorNumber = "246")
public class OasisGardener extends Card {

    public OasisGardener() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
