package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetArtifactsThenConjurePerpetualCopiesIntoHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HBG", collectorNumber = "52")
public class FlamesOfMoradin extends Card {

    public FlamesOfMoradin() {
        target(TargetFilters.artifact(), 0, 3)
                .addEffect(EffectSlot.SPELL, new DestroyTargetArtifactsThenConjurePerpetualCopiesIntoHandEffect());
    }
}
