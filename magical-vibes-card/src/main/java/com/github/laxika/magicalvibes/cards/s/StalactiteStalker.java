package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.DescendedThisTurn;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "122")
public class StalactiteStalker extends Card {

    public StalactiteStalker() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new DescendedThisTurn(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{B}",
                List.of(
                        new SacrificeSelfCost(true),
                        new BoostTargetCreatureEffect(
                                new Scaled(new XValue(), -1),
                                new Scaled(new XValue(), -1))),
                "{2}{B}, Sacrifice this creature: Target creature gets -X/-X until end of turn, "
                        + "where X is this creature's power."
        ));
    }
}
