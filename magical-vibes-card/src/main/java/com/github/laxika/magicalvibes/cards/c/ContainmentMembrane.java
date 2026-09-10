package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "52")
public class ContainmentMembrane extends Card {

    public ContainmentMembrane() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());
    }
}
