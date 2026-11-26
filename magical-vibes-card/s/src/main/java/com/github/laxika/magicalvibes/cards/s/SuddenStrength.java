package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JUD", collectorNumber = "132")
@CardRegistration(set = "VMA", collectorNumber = "233")
public class SuddenStrength extends Card {

    public SuddenStrength() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 3))
                .addSpellEffect(new DrawCardEffect(1));
    }
}
