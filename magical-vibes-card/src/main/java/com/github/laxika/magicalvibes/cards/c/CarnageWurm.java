package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

/**
 * Bloodthirst 3 (CR 702.54a) is a static ability that reads "If an opponent was dealt damage this
 * turn, this permanent enters with three +1/+1 counters on it", so it is an as-enters replacement
 * rather than an ETB trigger. Trample is loaded from Scryfall.
 */
@CardRegistration(set = "M12", collectorNumber = "168")
public class CarnageWurm extends Card {

    public CarnageWurm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new OpponentDealtDamageThisTurn(1),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3))));
    }
}
