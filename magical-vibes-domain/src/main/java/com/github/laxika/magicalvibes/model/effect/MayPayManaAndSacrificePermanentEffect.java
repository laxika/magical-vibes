package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Resolution-time choice to pay mana and sacrifice one matching permanent before the follow-up
 * effect is put on the stack.
 *
 * @param manaCost mana cost to pay
 * @param filter permanents that may be sacrificed
 * @param thenEffect effect put on the stack after the sacrifice
 * @param permanentDescription human-readable description of the sacrifice choice
 */
public record MayPayManaAndSacrificePermanentEffect(
        String manaCost,
        PermanentPredicate filter,
        CardEffect thenEffect,
        String permanentDescription
) implements CardEffect {
}
