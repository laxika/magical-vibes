package com.github.laxika.magicalvibes.service.outcome;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithExileAndReturnTransformedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.ExileAndReturnTransformedService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Handles Liliana's Other Contract's game-loss replacement. */
@Component
@Slf4j
public class LilianaOtherContractLossReplacer implements LossReplacer {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileAndReturnTransformedService exileAndReturnTransformedService;

    public LilianaOtherContractLossReplacer(
            GameQueryService gameQueryService,
            GameLogService gameLogService,
            @Lazy ExileAndReturnTransformedService exileAndReturnTransformedService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.exileAndReturnTransformedService = exileAndReturnTransformedService;
    }

    @Override
    public boolean tryReplace(GameData gameData, UUID losingPlayerId, LossReason reason) {
        if (losingPlayerId == null) {
            return false;
        }

        Permanent contract = gameQueryService.findControlledPermanentWithStaticEffect(
                gameData, losingPlayerId, ReplaceControllerLossWithExileAndReturnTransformedEffect.class);
        if (contract == null || contract.isTransformed()) {
            return false;
        }

        if (!exileAndReturnTransformedService.exileAndReturnTransformed(
                gameData, contract.getId(), true)) {
            return false;
        }

        String playerName = gameData.playerIdToName.get(losingPlayerId);
        gameLogService.append(gameData, GameLog.text(
                playerName + " would lose the game — " + contract.getOriginalCard().getName()
                        + " returns transformed instead."));
        log.info("Game {} - {} loss ({}) replaced by {}",
                gameData.id, playerName, reason, contract.getOriginalCard().getName());
        return true;
    }
}
