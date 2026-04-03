package com.github.laxika.magicalvibes.model.effect;

/**
 * Produces mana of any color (player chooses) that can only be spent to cast
 * spells with flashback from a graveyard.
 * (e.g. Altar of the Lost: "{T}: Add two mana in any combination of colors.
 *  Spend this mana only to cast spells with flashback from a graveyard.")
 */
public record AwardFlashbackOnlyAnyColorManaEffect(int amount) implements ManaProducingEffect {
}
