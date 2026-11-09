package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent with the mana value of the spell that triggered the resolving ability: "destroy all permanents
 * with that spell's mana value" (Celestial Kirin). The spell is the resolving stack entry's triggering spell, read as
 * the ability resolves: the live spell while it is on the stack, else the entry's snapshot of it (CR 608.2h), its
 * mana value counting the X chosen for it. Read by the effects whose filter the engine binds to the entry's triggering
 * spell ({@code AmountEvaluationService.bindTriggeringSpellManaValue}); elsewhere, with no triggering spell, it
 * matches nothing.
 */
public record PermanentManaValueEqualsTriggeringSpellPredicate() implements PermanentPredicate {
}
