package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "77")
public class MerfolkSkyscout extends Card {

    public MerfolkSkyscout() {
        target(TargetFilters.permanent())
                .addTrigger(Triggers.attacks(), new UntapPermanentsEffect(TapUntapScope.TARGET))
                .addTrigger(Triggers.blocks(), TriggerMode.ONCE_PER_BLOCK,
                        new UntapPermanentsEffect(TapUntapScope.TARGET));
    }
}
