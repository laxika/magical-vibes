package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the owner of the spell that caused this trigger lose life equal to that spell's mana value, the stack
 * entry's event value.
 */
public record LoseLifeToOwnerOfTriggeringSpellEffect()
        implements TriggeringSpellManaValueEffect, TriggeringSpellReferencingEffect {
}
