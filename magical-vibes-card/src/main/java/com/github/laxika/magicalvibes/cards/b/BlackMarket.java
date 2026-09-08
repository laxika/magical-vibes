package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceCardEffect;

@CardRegistration(set = "MMQ", collectorNumber = "116")
public class BlackMarket extends Card {

    public BlackMarket() {
        // Whenever a creature dies, put a charge counter on this enchantment.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new PutCountersOnSourceCardEffect(CounterType.CHARGE));

        // At the beginning of your first main phase, add {B} for each charge counter on this enchantment.
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new AwardManaEffect(ManaColor.BLACK, new CountersOnSource(CounterType.CHARGE)));
    }
}
