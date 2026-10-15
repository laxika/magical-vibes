package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MRD", collectorNumber = "37")
public class InertiaBubble extends Card {

    public InertiaBubble() {
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());
    }
}
