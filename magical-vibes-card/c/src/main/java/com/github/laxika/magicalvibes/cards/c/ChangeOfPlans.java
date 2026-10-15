package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConniveEachTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutChosenTargetCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "NCC", collectorNumber = "24")
@CardRegistration(set = "NCC", collectorNumber = "125")
public class ChangeOfPlans extends Card {

    public ChangeOfPlans() {
        targetExactlyX(TargetFilters.creatureYouControl(), Integer.MAX_VALUE)
                .addEffect(EffectSlot.SPELL, new ConniveEachTargetEffect())
                .addEffect(EffectSlot.SPELL, new PhaseOutChosenTargetCreaturesEffect());
    }
}
