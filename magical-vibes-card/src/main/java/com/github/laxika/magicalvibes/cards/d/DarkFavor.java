package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "89")
public class DarkFavor extends Card {

    public DarkFavor() {
        // Enchant creature; on entry the Aura's controller loses 1 life, and the enchanted creature gets +3/+1.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(1))
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
