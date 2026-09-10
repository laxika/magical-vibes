package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

/**
 * Craft with Pride, the prepare spell of Goblin Glasswright // Craft with Pride (SOS 117).
 */
public class CraftWithPride extends Card {

    public CraftWithPride() {
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofTreasureToken(1));
    }
}
