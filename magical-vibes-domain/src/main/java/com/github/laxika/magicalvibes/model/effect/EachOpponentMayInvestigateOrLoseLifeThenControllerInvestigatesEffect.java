package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * Offers each opponent the choice to investigate; opponents who decline lose 1 life, then the
 * effect's controller investigates once plus once for each opponent who accepted.
 */
public record EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect(
        List<UUID> remainingOpponentIds,
        UUID controllerId,
        int investigatedOpponentCount
) implements CardEffect {

    public EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect() {
        this(List.of(), null, 0);
    }

    public EachOpponentMayInvestigateOrLoseLifeThenControllerInvestigatesEffect {
        remainingOpponentIds = List.copyOf(remainingOpponentIds);
    }
}
