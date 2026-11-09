package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card with the mana value of the spell that triggered the resolving ability: "discards all cards with that
 * spell's mana value" (Infernal Kirin). The spell is the resolving stack entry's triggering spell, read as the ability
 * resolves: the live spell while it is on the stack, else the entry's snapshot of it (CR 608.2h), its mana value
 * counting the X chosen for it. Read by the effects whose filter the engine binds to the entry's triggering spell
 * ({@code AmountEvaluationService.bindTriggeringSpellManaValue}); elsewhere, with no triggering spell, it matches
 * nothing.
 */
public record CardManaValueEqualsTriggeringSpellPredicate() implements CardPredicate {
}
