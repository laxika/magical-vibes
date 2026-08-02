package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CHK", collectorNumber = "76")
public class MysticRestraints extends Card {

    public MysticRestraints() {
        target(TargetFilters.creature())
                // When this Aura enters, tap enchanted creature.
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.TARGET))
                // Enchanted creature doesn't untap during its controller's untap step.
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());
    }
}
