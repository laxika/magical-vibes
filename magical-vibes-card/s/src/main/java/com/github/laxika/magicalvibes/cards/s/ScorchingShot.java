package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTJ", collectorNumber = "145")
public class ScorchingShot extends Card {

    public ScorchingShot() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(5));
    }
}
