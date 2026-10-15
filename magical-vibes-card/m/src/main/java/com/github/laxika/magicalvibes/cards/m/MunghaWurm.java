package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticOrbEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "PCY", collectorNumber = "119")
public class MunghaWurm extends Card {

    public MunghaWurm() {
        addEffect(EffectSlot.STATIC, new StaticOrbEffect(1, new PermanentIsLandPredicate(), false));
    }
}
