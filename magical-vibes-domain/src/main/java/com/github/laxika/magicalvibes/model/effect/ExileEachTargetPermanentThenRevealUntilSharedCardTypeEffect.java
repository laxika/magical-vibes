package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Exiles the chosen permanents simultaneously, then replaces them one at a time using their
 * last-known card types. Each controller chooses the order of their own replacements.
 */
public record ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect(
        List<Replacement> remaining, UUID selectedPermanentId) implements RemovalEffect {

    public ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect {
        remaining = remaining == null ? null : List.copyOf(remaining);
    }

    public ExileEachTargetPermanentThenRevealUntilSharedCardTypeEffect() {
        this(null, null);
    }

    /** Immutable characteristics and controller captured before the simultaneous exile event. */
    public record Replacement(UUID permanentId, UUID controllerId, String name, Set<CardType> cardTypes) {
        public Replacement {
            cardTypes = Set.copyOf(cardTypes);
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return remaining == null ? TargetSpec.harmful(TargetPredicates.permanent()) : TargetSpec.NONE;
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
