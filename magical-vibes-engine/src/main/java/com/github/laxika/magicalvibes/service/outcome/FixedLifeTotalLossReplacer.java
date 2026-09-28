package com.github.laxika.magicalvibes.service.outcome;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithExileAndLifeTotalEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Handles fixed-life-total game-loss replacements such as The Golden Throne. */
@Component
@Slf4j
public class FixedLifeTotalLossReplacer implements LossReplacer {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final LifeSupport lifeSupport;
    private final PermanentRemovalService permanentRemovalService;

    public FixedLifeTotalLossReplacer(GameQueryService gameQueryService,
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

        Permanent source = gameQueryService.findControlledPermanentWithStaticEffect(
                gameData, losingPlayerId, ReplaceControllerLossWithExileAndLifeTotalEffect.class);
        if (source == null) {
            return false;
        }

        String playerName = gameData.playerIdToName.get(losingPlayerId);
        String sourceName = source.getCard().getName();
        int lifeTotal = source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(ReplaceControllerLossWithExileAndLifeTotalEffect.class::isInstance)
                .map(ReplaceControllerLossWithExileAndLifeTotalEffect.class::cast)
                .mapToInt(ReplaceControllerLossWithExileAndLifeTotalEffect::lifeTotal)
                .findFirst()
                .orElseThrow();
        if (!permanentRemovalService.removePermanentToExile(gameData, source)) {
            return false;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        gameLogService.append(gameData, GameLog.text(
                playerName + " would lose the game — " + sourceName + " is exiled instead."));
        lifeSupport.applySetLifeTotal(gameData, losingPlayerId, lifeTotal);
        gameLogService.append(gameData, GameLog.text(
                playerName + "'s life total becomes " + gameData.getLife(losingPlayerId) + "."));
        log.info("Game {} - {} loss ({}) replaced by {}", gameData.id, playerName, reason, sourceName);
        return true;
    }
}
