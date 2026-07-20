package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "226")
public class EdificeOfAuthority extends Card {

    public EdificeOfAuthority() {
        // {1}, {T}: Target creature can't attack this turn. Put a brick counter on this artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new LockTargetPermanentEffect(true, false, false, EffectDuration.UNTIL_END_OF_TURN),
                        new PutCountersOnSelfEffect(CounterType.BRICK)
                ),
                "{1}, {T}: Target creature can't attack this turn. Put a brick counter on Edifice of Authority."
        ));

        // {1}, {T}: Until your next turn, target creature can't attack or block and its activated
        // abilities can't be activated. Activate only if there are three or more brick counters.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new LockTargetPermanentEffect(true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN)),
                "{1}, {T}: Until your next turn, target creature can't attack or block and its activated abilities can't be activated. Activate only if there are three or more brick counters on Edifice of Authority."
        ).withRequiredSourceCounters(CounterType.BRICK, 3));
    }
}
