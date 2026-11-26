package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WOE", collectorNumber = "110")
public class SugarRush extends Card {

    public SugarRush() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 0))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
