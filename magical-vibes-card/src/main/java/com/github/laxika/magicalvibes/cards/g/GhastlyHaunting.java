package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;

/**
 * Ghastly Haunting — back face of Soul Seizer.
 * Enchant creature. You control enchanted creature.
 */
public class GhastlyHaunting extends Card {

    public GhastlyHaunting() {
        addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
