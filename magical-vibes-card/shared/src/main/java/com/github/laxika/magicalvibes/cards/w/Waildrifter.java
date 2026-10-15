package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;

/**
 * Waildrifter — back face of Galedrifter.
 * Flying is auto-loaded from Scryfall keywords.
 */
public class Waildrifter extends Card {

    public Waildrifter() {
        // If Waildrifter would be put into a graveyard from anywhere, exile it instead.
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }
}
