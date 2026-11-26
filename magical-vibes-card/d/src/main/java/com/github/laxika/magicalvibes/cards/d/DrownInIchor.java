package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONE", collectorNumber = "91")
public class DrownInIchor extends Card {

    public DrownInIchor() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-4, -4))
                .addSpellEffect(new ProliferateEffect());
    }
}
