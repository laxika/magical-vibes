package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "295")
public class ArmageddonClock extends Card {

    public ArmageddonClock() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSelfEffect(CounterType.DOOM));

        addEffect(EffectSlot.DRAW_TRIGGERED, new DealDamageToPlayersEffect(
                new CountersOnSource(CounterType.DOOM), DamageRecipient.EACH_PLAYER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new RemoveCounterFromSourceEffect(CounterType.DOOM, 1)),
                "{4}: Remove a doom counter from Armageddon Clock. Any player may activate this ability but only during any upkeep step.",
                ActivationTimingRestriction.ONLY_DURING_ANY_UPKEEP
        ).withActivatableByAnyPlayer());
    }
}
