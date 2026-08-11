package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "105")
public class Scourgemark extends Card {

    public Scourgemark() {
        // Enchant creature
        target(TargetFilters.creature())
                // When this Aura enters, draw a card.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1))
                // Enchanted creature gets +1/+0.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.ENCHANTED_CREATURE));
    }
}
