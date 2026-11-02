package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "125")
public class MolderSlug extends Card {

    public MolderSlug() {
        // At the beginning of each player's upkeep, that player sacrifices an artifact of their choice.
        addTrigger(Triggers.upkeep(ActivePlayer.ANY), new SacrificePermanentsEffect(
                1,
                new PermanentIsArtifactPredicate(),
                SacrificeRecipient.ACTIVE_PLAYER));
    }
}
