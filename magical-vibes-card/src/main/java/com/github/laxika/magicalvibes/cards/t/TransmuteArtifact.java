package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TransmuteArtifactSearchEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "ATQ", collectorNumber = "14")
public class TransmuteArtifact extends Card {

    public TransmuteArtifact() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentThenEffect(
                new PermanentIsArtifactPredicate(),
                new TransmuteArtifactSearchEffect(),
                "an artifact",
                false,
                false));
    }
}
