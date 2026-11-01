package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "62")
@CardRegistration(set = "SLD", collectorNumber = "155")
public class RoilElemental extends Card {

    public RoilElemental() {
        // Landfall — Whenever a land you control enters, you may gain control of target creature
        // for as long as you control this creature.
        target(TargetFilters.creature()).addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new MayEffect(
                        new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD),
                        "Gain control of target creature?"
                ));
    }
}
