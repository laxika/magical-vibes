package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M19", collectorNumber = "140")
public class FieryFinish extends Card {

    public FieryFinish() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(7));
    }
}
