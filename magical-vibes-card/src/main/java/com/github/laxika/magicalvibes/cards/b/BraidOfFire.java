package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;

import java.util.List;

@CardRegistration(set = "CSP", collectorNumber = "78")
public class BraidOfFire extends Card {

    public BraidOfFire() {
        // Cumulative upkeep — Add {R}: the zero-mana payment is always available, while the paid
        // effect produces one red mana for each age counter.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, CumulativeUpkeepEffect.withPaidEffects(
                "{0}", List.of(new AwardManaEffect(ManaColor.RED, new CountersOnSource(CounterType.AGE)))));
    }
}
