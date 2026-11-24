package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Permanently grants a subtype to the permanent chosen earlier during the same resolution.
 * The chosen permanent is stored on the resolving stack entry. Built only by the amass-Goblins continuation
 * ({@code CompleteAmassGoblinsEffect}); a card's "it becomes a [subtype] in addition to its other types" after a
 * choice is {@code GrantSubtypeEffect(subtype, Subjects.thisWay(StepKind.CHOOSE))}.
 */
public record GrantSubtypeToChosenPermanentEffect(CardSubtype subtype) implements CardEffect {
}
