package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.DealDamageToOneOfTwoTargetCreaturesThenOtherCantBlockEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "159")
public class TrialOfAgony extends Card {

    public TrialOfAgony() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL, new DealDamageToOneOfTwoTargetCreaturesThenOtherCantBlockEffect(5));
        target(TargetFilters.creatureAnOpponentControls());
        setMultiTargetConstraint(MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET);
    }
}
