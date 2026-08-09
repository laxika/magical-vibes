package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MRD", collectorNumber = "40")
public class LumengridSentinel extends Card {

    public LumengridSentinel() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD,
                new MayEffect(new TapPermanentsEffect(TapUntapScope.TARGET), "Tap target permanent?"));
    }
}
