package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "MSC", collectorNumber = "589")
public class CaptainMarvelShootingStar extends Card {

    public CaptainMarvelShootingStar() {
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect())
                .addEffect(EffectSlot.ON_ATTACK,
                        new ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect());

        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), Triggers.CREATURE, ObjectRelation.ANY),
                new GainLifeEffect(new EventValue()));
    }
}
