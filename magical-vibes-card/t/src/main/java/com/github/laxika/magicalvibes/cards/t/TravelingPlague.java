package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.AttachedBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToChosenCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "166")
public class TravelingPlague extends Card {

    public TravelingPlague() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.upkeep(ActivePlayer.ANY), new PutCountersOnSelfEffect(CounterType.PLAGUE))
                .addStaticEffect(new AttachedBoostEffect(
                        new Scaled(new CountersOnSource(CounterType.PLAGUE), -1),
                        new Scaled(new CountersOnSource(CounterType.PLAGUE), -1),
                        GrantScope.ENCHANTED_CREATURE))
                .addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ENCHANTED),
                        new ReturnSourceAuraToChosenCreatureOnLeaveEffect());
    }
}
