package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryEffect;

@CardRegistration(set = "FRF", collectorNumber = "106")
public class LightningShrieker extends Card {

    public LightningShrieker() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ShuffleSelfIntoOwnerLibraryEffect());
    }
}
