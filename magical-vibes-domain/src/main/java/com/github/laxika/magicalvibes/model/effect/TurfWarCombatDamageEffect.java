package com.github.laxika.magicalvibes.model.effect;

/** Resolves Turf War's choice to take control of a contested land. */
public record TurfWarCombatDamageEffect(boolean damagedPlayerIsController)
        implements CardEffect {
}
