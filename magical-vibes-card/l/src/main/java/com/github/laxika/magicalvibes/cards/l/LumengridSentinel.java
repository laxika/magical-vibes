package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "40")
public class LumengridSentinel extends Card {

    public LumengridSentinel() {
        target(TargetFilters.permanent()).addTrigger(Triggers.enters(Triggers.ARTIFACT,
                ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(new TapPermanentsEffect(TapUntapScope.TARGET), "Tap target permanent?"));
    }
}
