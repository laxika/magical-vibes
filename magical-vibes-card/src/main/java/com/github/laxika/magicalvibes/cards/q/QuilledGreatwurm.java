package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.RemoveCountersFromControlledCreaturesCastingCost;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "111")
public class QuilledGreatwurm extends Card {

    public QuilledGreatwurm() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new ControllerTurn(),
                new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE,
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()),
                        GrantScope.ALL_OWN_CREATURES)));
        addCastingOption(new GraveyardCast(List.of(
                new RemoveCountersFromControlledCreaturesCastingCost(6, CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
