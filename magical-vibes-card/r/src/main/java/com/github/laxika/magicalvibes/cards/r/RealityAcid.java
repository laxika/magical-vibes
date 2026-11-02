package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterAndSacrificeSelfOnLastEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "45")
@CardRegistration(set = "TSR", collectorNumber = "81")
public class RealityAcid extends Card {

    public RealityAcid() {
        target(TargetFilters.permanent())
                .addStaticEffect(new EnterWithCountersEffect(CounterType.TIME, new Fixed(3)))
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                        new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME))
                .addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS),
                        new SacrificeEnchantedCreatureOnLeaveEffect());
    }
}
