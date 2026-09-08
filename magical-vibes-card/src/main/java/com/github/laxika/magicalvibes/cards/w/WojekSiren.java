package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "37")
public class WojekSiren extends Card {

    public WojekSiren() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL,
                        new BoostTargetAndSharingCreaturesUntilEndOfTurnEffect(1, 1));
    }
}
