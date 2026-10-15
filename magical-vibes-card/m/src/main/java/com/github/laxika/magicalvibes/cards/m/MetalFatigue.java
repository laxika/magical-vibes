package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "DST", collectorNumber = "8")
public class MetalFatigue extends Card {

    public MetalFatigue() {
        // Tap all artifacts.
        addEffect(EffectSlot.SPELL, new TapPermanentsEffect(
                TapUntapScope.ALL_PERMANENTS,
                new PermanentIsArtifactPredicate()));
    }
}
