package com.github.laxika.magicalvibes.model.effect;

/**
 * "Whenever enchanted land is tapped for mana, its controller adds …" (the trigger {@code
 * Triggers.enchantedLandTappedForMana()}): the land's controller adds the mana described by {@code mana} (e.g.
 * {@link AwardManaEffect}, {@link AwardAnyColorManaEffect}, {@link AwardChosenColorManaEffect}), resolved at once as a
 * triggered mana ability (CR 605.4a).
 */
public record AddManaOnEnchantedLandTapEffect(ManaProducingEffect mana) implements TriggeredManaAbilityEffect {
}
