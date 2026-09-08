package com.github.laxika.magicalvibes.service.outcome;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithExileAndStartingLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handles Exquisite Archangel's one-shot game-loss replacement.
 */
@Component
@Slf4j
public class ExquisiteArchangelLossReplacer implements LossReplacer {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;
    private final PermanentRemovalService permanentRemovalService;

    public ExquisiteArchangelLossReplacer(GameQueryService gameQueryService,
                                          GameLogService gameLogService,
                                          LifeSupport lifeSupport,
                                          @Lazy PermanentRemovalService permanentRemovalService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.lifeSupport = lifeSupport;
        this.permanentRemovalService = permanentRemovalService;
    }

    @Override
    public boolean tryReplace(GameData gameData, UUID losingPlayerId, LossReason reason) {
        if (losingPlayerId == null) {
            return false;
        }

        Permanent angel = gameQueryService.findControlledPermanentWithStaticEffect(
                gameData, losingPlayerId, ReplaceControllerLossWithExileAndStartingLifeEffect.class);
        if (angel == null) {
            return false;
        }

        String playerName = gameData.playerIdToName.get(losingPlayerId);
        String angelName = angel.getCard().getName();
        if (!permanentRemovalService.removePermanentToExile(gameData, angel)) {
            return false;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        gameLogService.append(gameData, GameLog.text(
                playerName + " would lose the game — " + angelName + " is exiled instead."));
        lifeSupport.applySetLifeTotal(gameData, losingPlayerId, GameData.STARTING_LIFE_TOTAL);
        gameLogService.append(gameData, GameLog.text(
                playerName + "'s life total becomes " + gameData.getLife(losingPlayerId) + "."));
        log.info("Game {} - {} loss ({}) replaced by {}", gameData.id, playerName, reason, angelName);
        return true;
    }
}
