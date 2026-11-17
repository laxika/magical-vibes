package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;
import java.util.UUID;

/**
 * Separates each defending player's nonflying creatures into two piles and restricts each
 * attacker to blockers with flying or in the pile chosen for that attacker this combat.
 *
 * <p>The resolving effect has no chosen pile. Each attacker's choice is a floating effect on that attacker that lasts
 * until end of combat (CR 611.2a) and carries the chosen pile's creature ids as {@code chosenGroundPile}.
 *
 * @param chosenGroundPile the nonflying creatures that may block the attacker this effect is on, besides fliers
 */
public record RagingRiverEffect(Set<UUID> chosenGroundPile) implements CardEffect {

    public RagingRiverEffect {
        chosenGroundPile = Set.copyOf(chosenGroundPile);
    }

    public RagingRiverEffect() {
        this(Set.of());
    }
}
