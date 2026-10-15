package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetAndTheirCreaturesEffect;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "106")
public class HeartOfBogardan extends Card {

    public HeartOfBogardan() {
        // Cumulative upkeep {2}, plus the companion trigger "When a player doesn't pay this
        // enchantment's cumulative upkeep, this enchantment deals X damage to target player or
        // planeswalker and each creature that player or that planeswalker's controller controls,
        // where X is twice the number of age counters on this enchantment minus 2". The unpaid
        // branch sacrifices the enchantment first, so the age-relative amount is snapshotted while
        // it is still on the battlefield (CR 608.2h).
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CumulativeUpkeepEffect.withUnpaidEffects("{2}",
                List.of(new DealDamageToTargetAndTheirCreaturesEffect(
                        new Sum(new Scaled(new CountersOnSource(CounterType.AGE), 2), new Fixed(-2))))));
    }
}
