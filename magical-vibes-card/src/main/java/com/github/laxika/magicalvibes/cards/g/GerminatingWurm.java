package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "187")
public class GerminatingWurm extends Card {

    public GerminatingWurm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{G}"))));
    }
}
