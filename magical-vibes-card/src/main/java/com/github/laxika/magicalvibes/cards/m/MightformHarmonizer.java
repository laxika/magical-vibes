package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "200")
public class MightformHarmonizer extends Card {

    public MightformHarmonizer() {
        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new BoostTargetCreatureEffect(new TargetPower(), new Fixed(0)));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{G}"))));
    }
}
