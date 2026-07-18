package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCappedCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceAtEndOfCombatEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "356")
@CardRegistration(set = "4ED", collectorNumber = "307")
public class ClockworkBeast extends Card {

    public ClockworkBeast() {
        // This creature enters with seven +1/+0 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ZERO, new Fixed(7)));

        // At end of combat, if this creature attacked or blocked this combat, remove a +1/+0 counter
        // from it. Scheduling only from ON_ATTACK/ON_BLOCK encodes the "attacked or blocked" condition.
        addEffect(EffectSlot.ON_ATTACK,
                new RemoveCounterFromSourceAtEndOfCombatEffect(CounterType.PLUS_ONE_PLUS_ZERO));
        addEffect(EffectSlot.ON_BLOCK,
                new RemoveCounterFromSourceAtEndOfCombatEffect(CounterType.PLUS_ONE_PLUS_ZERO));

        // {X}, {T}: Put up to X +1/+0 counters on this creature. This ability can't cause the total
        // number of +1/+0 counters on this creature to be greater than seven. Activate only during
        // your upkeep.
        addActivatedAbility(new ActivatedAbility(true, "{X}",
                List.of(new PutCappedCountersOnSourceEffect(CounterType.PLUS_ONE_PLUS_ZERO, new XValue(), 7)),
                "{X}, {T}: Put up to X +1/+0 counters on this creature. This ability can't cause the "
                        + "total number of +1/+0 counters on this creature to be greater than seven. "
                        + "Activate only during your upkeep.",
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP));
    }
}
