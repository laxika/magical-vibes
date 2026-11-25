package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FRF", collectorNumber = "92")
public class BatheInDragonfire extends Card {

    public BatheInDragonfire() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(4));
    }
}
