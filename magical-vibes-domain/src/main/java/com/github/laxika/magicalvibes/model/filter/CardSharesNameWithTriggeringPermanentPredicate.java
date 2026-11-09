package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card with the same name as the permanent that triggered the resolving ability: "search their library for a
 * card with the same name" as the creature that died (Verdant Succession). The permanent is the resolving stack
 * entry's triggering permanent, read as the ability resolves: the permanent as it is, else as it last existed on the
 * battlefield (CR 608.2h). A face-down permanent has no name, so it matches nothing. The effects whose filter the engine
 * binds to the entry ({@code AmountEvaluationService.bindTriggeringPermanentName}, the library search) read it;
 * elsewhere, with no triggering permanent, it matches nothing.
 */
public record CardSharesNameWithTriggeringPermanentPredicate() implements CardPredicate {
}
