package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "THB", collectorNumber = "172")
public class HydrasGrowth extends Card {

    public HydrasGrowth() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new PutCountersEffect(Subjects.attached(), CounterType.PLUS_ONE_PLUS_ONE))
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        new DoublePlusOneCountersOnTargetCreatureEffect(Subjects.attached()));
    }
}
