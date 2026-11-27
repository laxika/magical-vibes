package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared prevention/shield helpers used by every "normal" Prevention effect handler.
 *
 * <p>Extracted verbatim from {@code PreventionResolutionService}; behavior is identical.
 */
@Component
@RequiredArgsConstructor
public class PreventionSupport {

    private final GameLogService gameLogService;

    /**
     * {@code permanentId} assigns no combat damage for {@code duration}: not a prevention effect, so "damage can't be
     * prevented" doesn't override it (what a creature assigns is CR 510.1a).
     */
    public static void assignNoCombatDamage(GameData gameData, String sourceCardName, UUID sourcePermanentId,
                                            UUID controllerId, UUID permanentId, Duration duration) {
        gameData.addFloatingEffect(FloatingContinuousEffect.onPermanent(sourceCardName, sourcePermanentId,
                controllerId, new AssignNoCombatDamageEffect(), permanentId, duration));
    }

    /**
     * "All combat damage {@code permanentId} would deal is prevented" for {@code duration} (CR 615): the floating effect
     * every card that has a creature deal no combat damage makes.
     */
    public static void preventCombatDamageDealtBy(GameData gameData, String sourceCardName, UUID sourcePermanentId,
                                                  UUID controllerId, UUID permanentId, Duration duration) {
        gameData.addFloatingEffect(FloatingContinuousEffect.onPermanent(sourceCardName, sourcePermanentId,
                controllerId, PreventDamageEffect.allCombatBySelf(), permanentId, duration));
    }

    /**
     * "All combat damage that would be dealt to {@code permanentId} is prevented" for {@code duration} (CR 615).
     */
    public static void preventCombatDamageDealtTo(GameData gameData, String sourceCardName, UUID sourcePermanentId,
                                                  UUID controllerId, UUID permanentId, Duration duration) {
        gameData.addFloatingEffect(FloatingContinuousEffect.onPermanent(sourceCardName, sourcePermanentId,
                controllerId, PreventDamageEffect.allCombatToSelf(), permanentId, duration));
    }

    public List<UUID> collectAllBattlefieldPermanentIds(GameData gameData) {
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> validIds.add(perm.getId()));
        return validIds;
    }

    public void broadcastNoPermanentsForDamageSourceChoice(GameData gameData) {
        String logEntry = "No permanents on the battlefield to choose as a damage source.";
        gameLogService.append(gameData, GameLog.text(logEntry));
    }

    public void broadcastNoDamageSourcesForChoice(GameData gameData) {
        gameLogService.append(gameData, GameLog.text("No damage sources to choose from."));
    }
}
