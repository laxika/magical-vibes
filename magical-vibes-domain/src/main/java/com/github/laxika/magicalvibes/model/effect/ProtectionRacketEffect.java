package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Protection Racket's upkeep process: each opponent may pay the revealed card's mana value in
 * life to exile it; otherwise that card is put into its owner's hand.
 */
public record ProtectionRacketEffect(
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        UUID sourcePermanentId,
        UUID revealedCardId,
        int manaValue
) implements CardEffect {

    public ProtectionRacketEffect() {
        this(List.of(), null, null, null, 0);
    }

    public ProtectionRacketEffect {
        remainingOpponentIds = remainingOpponentIds == null ? List.of() : List.copyOf(remainingOpponentIds);
    }
}
