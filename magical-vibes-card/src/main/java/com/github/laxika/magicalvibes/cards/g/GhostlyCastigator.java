package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;

/**
 * Ghostly Castigator — back face of Covetous Castaway.
 * Flying is auto-loaded from Scryfall keywords.
 */
public class GhostlyCastigator extends Card {

    public GhostlyCastigator() {
        // When this creature enters, you may shuffle up to three target cards from your
        // graveyard into your library. ("up to" covers "you may")
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(null, 3));

        // If Ghostly Castigator would be put into a graveyard from anywhere, exile it instead.
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }
}
