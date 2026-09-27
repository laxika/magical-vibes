package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "40K", collectorNumber = "111")
public class ChaosMutation extends Card {

    public ChaosMutation() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.creature(), 0, 99)
                .addEffect(EffectSlot.SPELL,
                        new ExileTargetCreaturesThenRevealUntilCreatureToBattlefieldRestOnBottomRandomEffect());
    }
}
