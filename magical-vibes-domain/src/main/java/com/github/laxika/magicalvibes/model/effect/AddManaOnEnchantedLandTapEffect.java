package com.github.laxika.magicalvibes.model.effect;

/**
 * When enchanted land is tapped for mana, its controller adds the mana described by
 * {@code mana} (e.g. {@link AwardManaEffect}, {@link AwardAnyColorManaEffect}).
 */
public record AddManaOnEnchantedLandTapEffect(ManaProducingEffect mana) implements CardEffect {
}
