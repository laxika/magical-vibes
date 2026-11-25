package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JUD", collectorNumber = "101")
public class Swelter extends Card {

    public Swelter() {
        target(TargetFilters.creature(), 2, 2)
                .addSpellEffect(DealDamageEffect.toTargetCreature(2));
    }
}
