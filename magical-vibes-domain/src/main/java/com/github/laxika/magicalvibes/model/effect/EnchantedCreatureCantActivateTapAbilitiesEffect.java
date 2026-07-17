package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker: the enchanted creature's activated abilities that include {T} in their costs
 * can't be activated (Serra Bestiary). Unlike {@link EnchantedCreatureCantActivateAbilitiesEffect}
 * (Arrest), non-tap activated abilities remain usable. Queried by class in
 * {@code AbilityActivationService} for the mana-tap and {@code isRequiresTap()} activation paths.
 */
public record EnchantedCreatureCantActivateTapAbilitiesEffect() implements CardEffect {
}
