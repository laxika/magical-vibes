package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Objects;
import java.util.Set;

/**
 * One-shot effect: the target, every creature on the battlefield, or every creature controlled by
 * the source's controller becomes the creature type chosen at resolution until end of turn,
 * replacing all other creature types.
 */
public record TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(
        GrantScope scope,
        Set<CardSubtype> excludedSubtypes
) implements CardEffect {

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect() {
        this(GrantScope.TARGET, Set.of(CardSubtype.WALL));
    }

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(GrantScope scope) {
        this(scope, Set.of(CardSubtype.WALL));
    }

    public TargetCreatureBecomesChosenSubtypeUntilEndOfTurnEffect(GrantScope scope,
                                                                   Set<CardSubtype> excludedSubtypes) {
        this.scope = Objects.requireNonNull(scope, "scope");
        this.excludedSubtypes = Set.copyOf(Objects.requireNonNull(excludedSubtypes, "excludedSubtypes"));
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case OWN_CREATURES, ALL_CREATURES -> TargetSpec.NONE;
            default -> TargetSpec.benign(TargetPredicates.creature());
        };
    }
}
