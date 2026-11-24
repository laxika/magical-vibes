package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;

@CardRegistration(set = "DRC", collectorNumber = "15")
@CardRegistration(set = "DRC", collectorNumber = "31")
public class RampagingAetherhood extends Card {

    public RampagingAetherhood() {
        // Ward {2}.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessPaysEffect(2));

        // The energy gain and the optional payment are one ability, so they resolve in order.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(
                new EnergyCountersEffect(new SourcePower()),
                EachPlayerPaysAnyManaEffect.byYou(Resource.ENERGY),
                new ConditionalEffect(new HappenedThisWay(StepKind.PAY_ENERGY),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, AmountThisWay.energyPaid()))));
    }
}
