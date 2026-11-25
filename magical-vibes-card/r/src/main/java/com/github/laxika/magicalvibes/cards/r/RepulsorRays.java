package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MSC", collectorNumber = "552")
@CardRegistration(set = "MSC", collectorNumber = "854")
public class RepulsorRays extends Card {

    public RepulsorRays() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(3));
    }
}
