package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

/**
 * Bloodthirst 1 (CR 702.54a) is a static ability that reads "If an opponent was dealt damage this
 * turn, this permanent enters with a +1/+1 counter on it", so it is an as-enters replacement rather
 * than an ETB trigger. Islandwalk is loaded from Scryfall metadata.
 */
@CardRegistration(set = "M12", collectorNumber = "184")
public class LurkingCrocodile extends Card {

    public LurkingCrocodile() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new OpponentDealtDamageThisTurn(1),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1))));
    }
}
