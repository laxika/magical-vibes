package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "223")
@CardRegistration(set = "MSC", collectorNumber = "454")
public class TomeOfLegends extends Card {

    public TomeOfLegends() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PAGE, new Fixed(1)));

        PutCountersOnSelfEffect addPageCounter = new PutCountersOnSelfEffect(CounterType.PAGE);
        addEffect(EffectSlot.ON_ALLY_PERMANENT_ENTERS_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(new PermanentIsCommanderPredicate(), addPageCounter));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(new PermanentIsCommanderPredicate(), addPageCounter));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.PAGE),
                        new DrawCardEffect(1)
                ),
                "{1}, {T}, Remove a page counter from Tome of Legends: Draw a card."
        ));
    }
}
