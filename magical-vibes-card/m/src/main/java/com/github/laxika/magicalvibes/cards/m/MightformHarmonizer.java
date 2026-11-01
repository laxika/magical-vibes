package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "200")
public class MightformHarmonizer extends Card {

    public MightformHarmonizer() {
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.enters(Triggers.LAND,
                ObjectRelation.YOU_CONTROL),
                new BoostTargetCreatureEffect(new TargetPower(true), new Fixed(0)));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{2}{G}"))));
    }
}
