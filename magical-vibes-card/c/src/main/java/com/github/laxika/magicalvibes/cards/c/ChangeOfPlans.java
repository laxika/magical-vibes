package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PhaseOutChosenTargetCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;

@CardRegistration(set = "NCC", collectorNumber = "24")
@CardRegistration(set = "NCC", collectorNumber = "125")
public class ChangeOfPlans extends Card {

    public ChangeOfPlans() {
        targetExactlyX(TargetFilters.creatureYouControl(), Integer.MAX_VALUE)
                .addSpellEffect(DrawDiscardAndConniveEffect.of(Subjects.target()))
                .addSpellEffect(new PhaseOutChosenTargetCreaturesEffect());
    }
}
