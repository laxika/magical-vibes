package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MRD", collectorNumber = "125")
public class MolderSlug extends Card {

    public MolderSlug() {
        // At the beginning of each player's upkeep, that player sacrifices an artifact of their choice.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new SacrificePermanentsEffect(
                1,
                new PermanentIsArtifactPredicate(),
                SacrificeRecipient.TARGET_PLAYER));
    }
}
