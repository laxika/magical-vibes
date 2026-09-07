package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "183")
public class SpinewoodsPaladin extends Card {

    public SpinewoodsPaladin() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{3}{G}"))));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
    }
}
