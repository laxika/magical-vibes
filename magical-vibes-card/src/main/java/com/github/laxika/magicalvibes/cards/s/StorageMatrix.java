package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StorageMatrixEffect;

@CardRegistration(set = "9ED", collectorNumber = "310")
public class StorageMatrix extends Card {

    public StorageMatrix() {
        // Static: as long as this artifact is untapped, each player chooses artifact, creature, or
        // land during their untap step and can untap only permanents of the chosen type this step.
        addEffect(EffectSlot.STATIC, new StorageMatrixEffect());
    }
}
