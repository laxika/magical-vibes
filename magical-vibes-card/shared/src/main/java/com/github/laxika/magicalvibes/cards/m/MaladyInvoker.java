package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

public class MaladyInvoker extends Card {

    public MaladyInvoker() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE,
                        new BoostTargetCreatureEffect(new Fixed(0), new Scaled(new SourcePower(), -1)));
    }
}
