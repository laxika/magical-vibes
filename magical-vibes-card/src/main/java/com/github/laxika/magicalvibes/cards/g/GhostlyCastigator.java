package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;

/**
 * Ghostly Castigator — back face of Covetous Castaway.
 * Flying is auto-loaded from Scryfall keywords.
 */
public class GhostlyCastigator extends Card {

    public GhostlyCastigator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(null, 3),
                        "Shuffle the targeted cards into your library?"));

        // If Ghostly Castigator would be put into a graveyard from anywhere, exile it instead.
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }
}
