package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "8ED", collectorNumber = "231")
@CardRegistration(set = "9ED", collectorNumber = "226")
@CardRegistration(set = "POR", collectorNumber = "154")
@CardRegistration(set = "P02", collectorNumber = "119")
public class VolcanicHammer extends Card {

    public VolcanicHammer() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
