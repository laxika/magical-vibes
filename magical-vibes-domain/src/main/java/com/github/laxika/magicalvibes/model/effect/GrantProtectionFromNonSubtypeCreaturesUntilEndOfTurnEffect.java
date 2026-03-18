package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Grants all creatures the controller controls protection from creatures that do NOT
 * have the specified subtype until end of turn. For example, with {@code CardSubtype.HUMAN},
 * creatures gain "protection from non-Human creatures."
 *
 * <p>This is a mass grant effect (affects all your creatures, not a single target).
 * The protection prevents blocking, damage, targeting, and enchanting/equipping by
 * creature sources that lack the specified subtype. Changelings (which have all creature
 * subtypes) are NOT affected by this protection.
 */
public record GrantProtectionFromNonSubtypeCreaturesUntilEndOfTurnEffect(CardSubtype excludedSubtype) implements CardEffect {
}
