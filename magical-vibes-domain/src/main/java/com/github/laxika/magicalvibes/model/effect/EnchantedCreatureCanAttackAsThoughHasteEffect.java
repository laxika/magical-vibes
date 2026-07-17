package com.github.laxika.magicalvibes.model.effect;

/**
 * Static aura marker: the enchanted creature can attack as though it had haste (it ignores
 * summoning sickness for the purpose of attacking only). Unlike {@code GrantKeywordEffect(HASTE, …)}
 * this does NOT lift summoning sickness for {@code {T}} ability activation — it only grants attack
 * permission, matching cards like Instill Energy. Detected via
 * {@code GameQueryService.hasAuraWithEffect} in the combat attack-legality check.
 */
public record EnchantedCreatureCanAttackAsThoughHasteEffect() implements CardEffect {
}
