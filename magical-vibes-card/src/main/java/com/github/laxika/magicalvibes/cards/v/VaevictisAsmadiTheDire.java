package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentsThenRevealTopCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M19", collectorNumber = "225")
public class VaevictisAsmadiTheDire extends Card {

    public VaevictisAsmadiTheDire() {
        setMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER);
        target(TargetFilters.permanent(), 0, 99)
                .addEffect(EffectSlot.ON_ATTACK, new SacrificeTargetPermanentsThenRevealTopCardEffect());
    }
}
