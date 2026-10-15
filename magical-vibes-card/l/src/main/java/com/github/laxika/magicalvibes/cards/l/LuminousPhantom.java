package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileInsteadOfGraveyardReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

/**
 * Luminous Phantom — back face of Lunarch Veteran.
 * Flying is auto-loaded from Scryfall keywords.
 */
public class LuminousPhantom extends Card {

    public LuminousPhantom() {
        // Whenever another creature you control leaves the battlefield, you gain 1 life.
        addEffect(EffectSlot.ON_ALLY_CREATURE_LEAVES_BATTLEFIELD, new GainLifeEffect(1));

        // If Luminous Phantom would be put into a graveyard from anywhere, exile it instead.
        addEffect(EffectSlot.STATIC, new ExileInsteadOfGraveyardReplacementEffect());
    }
}
