package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "POR", collectorNumber = "173")
@CardRegistration(set = "P02", collectorNumber = "133")
@CardRegistration(set = "8ED", collectorNumber = "266")
public class MonstrousGrowth extends Card {

    public MonstrousGrowth() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 4));
    }
}
