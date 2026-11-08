package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card with the same name as the spell that triggered the resolving ability: "the number of cards in all
 * graveyards with the same name as that spell" (the Odyssey shrines). The spell is the resolving stack entry's
 * triggering spell, read as the ability resolves: the live spell while it is on the stack, else the entry's snapshot of
 * it (CR 608.2h). A face-down spell has no name, so it matches nothing. Read by the {@code CardsInGraveyard} count,
 * which has the resolving entry; elsewhere, with no triggering spell, it matches nothing.
 */
public record CardSharesNameWithTriggeringSpellPredicate() implements CardPredicate {
}
