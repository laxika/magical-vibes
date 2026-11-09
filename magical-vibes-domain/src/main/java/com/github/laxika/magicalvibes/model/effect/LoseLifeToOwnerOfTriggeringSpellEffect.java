package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the owner of the spell that caused this trigger lose life equal to that spell's mana value, read as the
 * ability resolves ({@code TargetSpellManaValue}: the spell, else as it last existed on the stack).
 */
public record LoseLifeToOwnerOfTriggeringSpellEffect()
        implements CardEffect {
}
