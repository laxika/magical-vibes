package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches the card the triggered ability's event was about ("it", "that card"): "return up to one other target creature
 * card" beside "exile it" of a creature that died (Illicit Masquerade). The choice of a target as the ability goes on
 * the stack binds it to the record's triggering card ({@code AmountEvaluationService.bindTriggeringCard}); elsewhere,
 * with no triggering card, it matches nothing.
 */
public record CardIsTriggeringCardPredicate() implements CardPredicate {
}
