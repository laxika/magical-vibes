package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "WAR", collectorNumber = "156")
public class CentaurNurturer extends Card {

    public CentaurNurturer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
