package com.github.laxika.magicalvibes.model.effect;

/**
 * Static ability of an Aura or Equipment that restricts the permanent it is attached to
 * ("Enchanted creature can't be the target of spells", "Equipped creature can't be sacrificed").
 * Unlike {@link GrantEffectEffect}, nothing is granted: the restriction stays the attachment's own
 * ability (CR 113.10 — only "has"/"gains" text adds an ability), so the attached permanent losing
 * its abilities in layer 6 (CR 613.1f) doesn't remove it, and the restriction never applies to the
 * Aura or Equipment itself.
 * <p>
 * The wrapped {@code restriction} is read by the query that owns that restriction kind:
 * {@link TargetingRestrictionEffect} (can't be the target of spells, and abilities from sources of
 * the given card types), {@link CantBeEnchantedByOtherAurasEffect} (the imposing Aura itself is not
 * "other"), {@link CantBeSacrificedEffect} and {@link CantBecomeSuspectedEffect}.
 *
 * @param restriction the restriction the attached permanent is under
 */
public record AttachedPermanentRestrictionEffect(CardEffect restriction) implements CardEffect {
}
