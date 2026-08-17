package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GRN", collectorNumber = "9")
public class Demotion extends Card {

    public Demotion() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new CantBlockEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantActivateAbilitiesEffect());
    }
}
