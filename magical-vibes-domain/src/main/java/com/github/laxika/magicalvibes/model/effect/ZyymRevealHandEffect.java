package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Resolves Zyym's ordered hand reveal and stop-or-reveal sequence. */
public record ZyymRevealHandEffect(
        UUID targetPlayerId,
        List<UUID> orderedCardIds,
        List<UUID> revealedCardIds,
        int nextCardIndex) implements CardEffect {

    public ZyymRevealHandEffect() {
        this(null, List.of(), List.of(), 0);
    }

    public ZyymRevealHandEffect {
        orderedCardIds = orderedCardIds == null ? List.of() : List.copyOf(orderedCardIds);
        revealedCardIds = revealedCardIds == null ? List.of() : List.copyOf(revealedCardIds);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
