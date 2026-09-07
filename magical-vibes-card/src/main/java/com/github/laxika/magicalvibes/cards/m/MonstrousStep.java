package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "IKO", collectorNumber = "166")
public class MonstrousStep extends Card {

    public MonstrousStep() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(7, 7));
        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.SPELL, new MustBlockTargetCreatureEffect(1, 0));

        addCycling("{2}");
    }
}
