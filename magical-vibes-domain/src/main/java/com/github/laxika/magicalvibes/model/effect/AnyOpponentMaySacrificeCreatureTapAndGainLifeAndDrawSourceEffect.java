package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Combat trigger (Clackbridge Troll): any opponent may sacrifice a creature of their choice. If a
 * player does, tap the source, its controller gains 3 life, and that player draws a card.
 *
 * <p>Every eligible opponent is offered the choice in APNAP order. The reward happens once after
 * all opponents have chosen if at least one opponent sacrificed a creature.
 */
public record AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect(
        List<UUID> remainingOpponentIds,
        UUID abilityControllerId,
        UUID sourcePermanentId,
        boolean anyAccepted
) implements CardEffect {

    public AnyOpponentMaySacrificeCreatureTapAndGainLifeAndDrawSourceEffect() {
        this(null, null, null, false);
    }
}
