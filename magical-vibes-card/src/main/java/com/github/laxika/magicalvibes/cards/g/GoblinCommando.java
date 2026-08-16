package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "S99", collectorNumber = "100")
public class GoblinCommando extends Card {

    public GoblinCommando() {
        target(TargetFilters.creature()).addEffect(
                EffectSlot.ON_ENTER_BATTLEFIELD,
                new DealDamageToTargetCreatureEffect(2));
    }
}
