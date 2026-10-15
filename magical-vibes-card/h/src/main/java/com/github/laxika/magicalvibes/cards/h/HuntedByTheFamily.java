package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.HuntedByTheFamilyEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "46")
@CardRegistration(set = "WHO", collectorNumber = "361")
@CardRegistration(set = "WHO", collectorNumber = "651")
public class HuntedByTheFamily extends Card {

    public HuntedByTheFamily() {
        target(TargetFilters.creatureAnOpponentControls(), 0, 4)
                .addEffect(EffectSlot.SPELL, new HuntedByTheFamilyEffect());
    }
}
