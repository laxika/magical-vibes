package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyEachTargetArtifactThenCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONE", collectorNumber = "145")
public class RedSunsTwilight extends Card {

    public RedSunsTwilight() {
        targetX(TargetFilters.artifact(), 100)
                .addEffect(EffectSlot.SPELL, new DestroyEachTargetArtifactThenCreateTokenCopyEffect());
    }
}
