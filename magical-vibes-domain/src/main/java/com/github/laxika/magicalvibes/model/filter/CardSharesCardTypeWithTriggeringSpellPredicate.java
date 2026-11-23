package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card that shares a card type with the spell that triggered the resolving ability: "a spell with lesser
 * mana value that shares a card type with it" (Baral and Kari Zev). The spell is the resolving stack entry's triggering
 * spell, read as the ability resolves: the live spell while it is on the stack, else the entry's snapshot of it
 * (CR 608.2h), with the characteristics effects see (a face-down spell is a creature spell). Read by the effects whose
 * filter the engine binds to the entry's triggering spell ({@code AmountEvaluationService.bindTriggeringSpellCardTypes});
 * elsewhere, with no triggering spell, it matches nothing.
 */
public record CardSharesCardTypeWithTriggeringSpellPredicate() implements CardPredicate {
}
