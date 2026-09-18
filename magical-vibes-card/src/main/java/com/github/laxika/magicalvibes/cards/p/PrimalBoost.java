package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "277")
@CardRegistration(set = "DMR", collectorNumber = "173")
public class PrimalBoost extends Card {

    public PrimalBoost() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 4));

        addCycling("{2}{G}");
        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(
                new BoostTargetCreatureEffect(1, 1), "Give target creature +1/+1 until end of turn?"));
    }
}
