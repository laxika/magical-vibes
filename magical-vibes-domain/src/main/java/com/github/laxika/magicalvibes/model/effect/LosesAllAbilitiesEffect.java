package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that causes permanents matching the given scope to lose all abilities.
 * Used by auras like Deep Freeze ("enchanted creature ... loses all other abilities").
 * Keywords, activated abilities, triggered abilities, and static abilities of the
 * affected permanent are suppressed while this effect is active.
 *
 * @param scope which permanents are affected (ENCHANTED_CREATURE, EQUIPPED_CREATURE, etc.)
 */
public record LosesAllAbilitiesEffect(GrantScope scope) implements CardEffect {
}
