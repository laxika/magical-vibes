package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Produces mana that can only be spent to cast kicked spells.
 * (e.g. Elfhame Druid: "{T}: Add {G}{G}. Spend this mana only to cast kicked spells.")
 */
public record AwardKickedOnlyManaEffect(ManaColor color, int amount) implements ManaProducingEffect {
}
