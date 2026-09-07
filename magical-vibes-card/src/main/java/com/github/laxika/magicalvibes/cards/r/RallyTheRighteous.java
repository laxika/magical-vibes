package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetAndSharingCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "222")
public class RallyTheRighteous extends Card {

    public RallyTheRighteous() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new UntapTargetAndSharingCreaturesEffect())
                .addEffect(EffectSlot.SPELL,
                        new BoostTargetAndSharingCreaturesUntilEndOfTurnEffect(2, 0));
    }
}
