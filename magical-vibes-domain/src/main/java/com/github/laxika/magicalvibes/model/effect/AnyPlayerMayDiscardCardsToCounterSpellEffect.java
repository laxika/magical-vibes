package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Cast trigger that offers each player the choice to discard a fixed number of cards and counters
 * the triggering spell when a player does. Remaining players still receive their choices.
 */
public record AnyPlayerMayDiscardCardsToCounterSpellEffect(
        int cardsToDiscard,
        List<UUID> remainingPlayerIds,
        UUID abilityControllerId,
        UUID targetCardId,
        boolean afterDiscard
) implements TriggeringSpellReferencingEffect, CounterSpellingEffect {

    public AnyPlayerMayDiscardCardsToCounterSpellEffect(int cardsToDiscard) {
        this(cardsToDiscard, List.of(), null, null, false);
    }

    public AnyPlayerMayDiscardCardsToCounterSpellEffect {
        remainingPlayerIds = remainingPlayerIds == null ? List.of() : List.copyOf(remainingPlayerIds);
    }
}
