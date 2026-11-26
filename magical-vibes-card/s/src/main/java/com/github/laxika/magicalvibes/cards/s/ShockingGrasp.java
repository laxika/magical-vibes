package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "72")
@CardRegistration(set = "HBG", collectorNumber = "132")
public class ShockingGrasp extends Card {

    public ShockingGrasp() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-2, 0))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
