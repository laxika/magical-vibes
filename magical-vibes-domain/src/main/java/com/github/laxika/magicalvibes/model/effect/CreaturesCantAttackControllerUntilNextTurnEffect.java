package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Resolves to a player-scoped restriction that lasts until the controller's next turn. */
public record CreaturesCantAttackControllerUntilNextTurnEffect(
        UUID restrictedAttackerId,
        boolean protectsPlaneswalkers
) implements CardEffect {

    public CreaturesCantAttackControllerUntilNextTurnEffect() {
        this(null, false);
    }
}
