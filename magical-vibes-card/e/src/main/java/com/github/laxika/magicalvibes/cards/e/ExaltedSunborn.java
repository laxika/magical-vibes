package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.MultiplyTokenCreationEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "15")
public class ExaltedSunborn extends Card {

    public ExaltedSunborn() {
        addEffect(EffectSlot.STATIC, new MultiplyTokenCreationEffect(2));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{W}"))));
    }
}
