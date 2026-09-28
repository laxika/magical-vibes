package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PIP", collectorNumber = "50")
@CardRegistration(set = "PIP", collectorNumber = "333")
@CardRegistration(set = "PIP", collectorNumber = "578")
@CardRegistration(set = "PIP", collectorNumber = "861")
public class VATS extends Card {

    public VATS() {
        setMultiTargetConstraint(MultiTargetConstraint.SHARE_TOUGHNESS);
        target(TargetFilters.creature(), 0, 99)
                .addEffect(EffectSlot.SPELL, new DestroyEachTargetPermanentEffect());
    }
}
