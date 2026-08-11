package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ECL", collectorNumber = "67")
public class RunAwayTogether extends Card {

    public RunAwayTogether() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, ReturnToHandEffect.target());
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
    }
}
