package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "117")
public class FlameSpill extends Card {

    public FlameSpill() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(4).excessToControllerInstead());
    }
}
