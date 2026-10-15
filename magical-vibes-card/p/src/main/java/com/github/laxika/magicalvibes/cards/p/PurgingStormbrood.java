package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AbsorbEssence;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.OmenCast;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TDM", collectorNumber = "213")
public class PurgingStormbrood extends Card {

    public PurgingStormbrood() {
        setBackFaceCard(new AbsorbEssence());
        addCastingOption(new OmenCast());

        target(TargetFilters.creature(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RemoveAllCountersFromTargetPermanentEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AbsorbEssence";
    }
}
