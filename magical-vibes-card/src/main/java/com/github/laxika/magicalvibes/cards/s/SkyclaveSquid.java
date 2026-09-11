package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;

@CardRegistration(set = "ZNR", collectorNumber = "82")
public class SkyclaveSquid extends Card {

    public SkyclaveSquid() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new CanAttackAsThoughNoDefenderEffect());
    }
}
