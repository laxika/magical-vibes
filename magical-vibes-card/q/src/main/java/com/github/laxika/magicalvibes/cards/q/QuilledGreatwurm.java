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
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "111")
@CardRegistration(set = "FDN", collectorNumber = "339")
@CardRegistration(set = "FDN", collectorNumber = "401")
@CardRegistration(set = "FDN", collectorNumber = "473")
public class QuilledGreatwurm extends Card {

    public QuilledGreatwurm() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE, new ConditionalEffect(
                new ControllerTurn(),
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,
                        CounterType.PLUS_ONE_PLUS_ONE, new EventValue())));
        addCastingOption(new GraveyardCast(List.of(
                RemoveCountersFromControlledCreaturesCastingCost.anyKind(6))));
    }
}
