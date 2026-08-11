package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "147")
public class WildCelebrants extends Card {

    public WildCelebrants() {
        // When this creature enters, you may destroy target artifact.
        target(TargetFilters.artifact()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DestroyTargetPermanentEffect(),
                "Destroy target artifact?"
        ));
    }
}
