package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RNA", collectorNumber = "270")
public class Ragefire extends Card {

    public Ragefire() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(3));
    }
}
