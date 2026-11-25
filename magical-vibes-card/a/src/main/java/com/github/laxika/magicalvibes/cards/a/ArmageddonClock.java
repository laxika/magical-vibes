package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "295")
@CardRegistration(set = "SUM", collectorNumber = "234")
@CardRegistration(set = "ATQ", collectorNumber = "37")
@CardRegistration(set = "3ED", collectorNumber = "234")
@CardRegistration(set = "ME4", collectorNumber = "180")
public class ArmageddonClock extends Card {

    public ArmageddonClock() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), PutCountersEffect.onSelf(CounterType.DOOM));

        addTrigger(Triggers.drawStep(ActivePlayer.YOU), new DealDamageToPlayersEffect(
                new CountersOnSource(CounterType.DOOM), Subjects.eachPlayer()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(RemoveCountersEffect.fromSelf(CounterType.DOOM, 1)),
                "{4}: Remove a doom counter from Armageddon Clock. Any player may activate this ability but only during any upkeep step.",
                ActivationTimingRestriction.ONLY_DURING_ANY_UPKEEP
        ).withActivatableByAnyPlayer());
    }
}
