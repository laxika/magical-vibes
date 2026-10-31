package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureWithExcessToControllerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "STX", collectorNumber = "111")
public class PigmentStorm extends Card {

    public PigmentStorm() {
        target(TargetFilters.creature())
                .addSpellEffect(new DealDamageToTargetCreatureWithExcessToControllerEffect(5, false));
    }
}
