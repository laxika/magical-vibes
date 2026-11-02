package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Ghostly Castigator — back face of Covetous Castaway.
 * Flying is auto-loaded from Scryfall keywords.
 */
public class GhostlyCastigator extends Card {

    public GhostlyCastigator() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(null, 3),
                        "Shuffle the targeted cards into your library?"));

        // If Ghostly Castigator would be put into a graveyard from anywhere, exile it instead.
        addStaticEffect(new ExileInsteadOfGraveyardReplacementEffect());
    }
}
