package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MRD", collectorNumber = "38")
public class LoomingHoverguard extends Card {

    public LoomingHoverguard() {
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutTargetOnTopOfLibraryEffect());
    }
}
