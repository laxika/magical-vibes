package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentsAndControllersCloakEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKC", collectorNumber = "17")
@CardRegistration(set = "MKC", collectorNumber = "328")
public class UnexplainedAbsence extends Card {

    public UnexplainedAbsence() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.nonlandPermanent(), 0, 99)
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentsAndControllersCloakEffect());
    }
}
