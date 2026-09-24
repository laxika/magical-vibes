package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Comparator;
import java.util.UUID;

/**
 * Static combat requirement for a creature to attack the opponent with the most life each combat.
 * The requirement is inactive while the source's controller controls a named exempting permanent.
 */
public record MustAttackOpponentWithMostLifeEffect(String exemptingCardName)
        implements CombatAttackRequirementEffect {

    private static final PermanentPredicate SOURCE = new PermanentIsSourceCardPredicate();

    @Override
    public PermanentPredicate affectedPredicate() {
        return SOURCE;
    }

    @Override
    public boolean isActive(GameData gameData, Permanent sourcePermanent) {
        UUID controllerId = gameData.findControllerOf(sourcePermanent);
        if (controllerId == null) {
            return false;
        }
        var battlefield = gameData.playerBattlefields.get(controllerId);
        return battlefield == null || battlefield.stream()
                .noneMatch(permanent -> exemptingCardName.equals(permanent.getCard().getName()));
    }

    @Override
    public UUID requiredAttackTargetId(GameData gameData, Permanent sourcePermanent) {
        UUID controllerId = gameData.findControllerOf(sourcePermanent);
        if (controllerId == null) {
            return null;
        }
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .max(Comparator.comparingInt(playerId ->
                        gameData.playerLifeTotals.getOrDefault(playerId, 0)))
                .orElse(null);
    }
}
