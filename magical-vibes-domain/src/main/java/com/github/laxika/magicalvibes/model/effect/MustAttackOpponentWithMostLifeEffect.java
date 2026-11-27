package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Static combat requirement for a creature to attack an opponent with the most life each combat (any of them
 * on a tie).
 * The requirement is inactive while the source's controller controls a permanent matching
 * {@code exemptingPermanent} (Galactus: "unless you control a creature named Silver Surfer, ...").
 */
public record MustAttackOpponentWithMostLifeEffect(PermanentPredicate exemptingPermanent)
        implements CombatAttackRequirementEffect {

    private static final PermanentPredicate SOURCE = new PermanentIsSourceCardPredicate();

    @Override
    public PermanentPredicate affectedPredicate() {
        return SOURCE;
    }

    @Override
    public PermanentPredicate exemptingControlledPermanent() {
        return exemptingPermanent;
    }

    /** Every opponent still in the game with the most life among them: attacking any of them satisfies it. */
    @Override
    public Set<UUID> requiredAttackTargetIds(GameData gameData, Permanent sourcePermanent) {
        UUID controllerId = gameData.findControllerOf(sourcePermanent);
        if (controllerId == null) {
            return Set.of();
        }
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId) && gameData.playerIds.contains(playerId))
                .toList();
        int mostLife = opponents.stream()
                .mapToInt(playerId -> gameData.playerLifeTotals.getOrDefault(playerId, 0))
                .max().orElse(0);
        return opponents.stream()
                .filter(playerId -> gameData.playerLifeTotals.getOrDefault(playerId, 0) == mostLife)
                .collect(Collectors.toUnmodifiableSet());
    }
}
