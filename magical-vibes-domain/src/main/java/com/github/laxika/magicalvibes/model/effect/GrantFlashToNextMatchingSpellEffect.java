package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

/**
 * "The next [matching] spell you cast this turn can be cast as though it had flash": as it resolves, a floating effect
 * on the controller until a spell matching {@code predicate} is cast or the turn ends. "The next sorcery spell"
 * (Quicken) is a {@code CardTypePredicate}; "the next spell" (Ride the Avalanche) is {@link #nextSpell()}; "the next
 * spell of the chosen type" (Progenitor's Icon) is {@code CardHasSourceChosenSubtypePredicate(false)}, whose type is
 * written in as it resolves ({@code SubjectResolutionService.bindChosenValues}).
 */
public record GrantFlashToNextMatchingSpellEffect(CardPredicate predicate) implements NextMatchingSpellEffect {

    /** "The next spell you cast this turn can be cast as though it had flash." */
    public static GrantFlashToNextMatchingSpellEffect nextSpell() {
        return new GrantFlashToNextMatchingSpellEffect(new CardTruePredicate());
    }
}
