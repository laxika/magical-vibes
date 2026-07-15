package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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

    private final GameBroadcastService gameBroadcastService;

    public List<UUID> collectAllBattlefieldPermanentIds(GameData gameData) {
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> validIds.add(perm.getId()));
        return validIds;
    }

    public void broadcastNoPermanentsForDamageSourceChoice(GameData gameData) {
        String logEntry = "No permanents on the battlefield to choose as a damage source.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
    }
}
