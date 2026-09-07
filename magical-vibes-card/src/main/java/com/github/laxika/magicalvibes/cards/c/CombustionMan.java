package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentUnlessControllerTakesDamageEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "127")
public class CombustionMan extends Card {

    public CombustionMan() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.ON_ATTACK,
                new DestroyTargetPermanentUnlessControllerTakesDamageEffect(new SourcePower()));
    }
}
