package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTargetPermanentToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetPermanentToHandEffect) effect;
        List<UUID> targetIds = entry.getTargetIds().isEmpty()
                ? List.of(entry.getTargetId())
                : entry.getTargetIds();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            UUID controllerId = e.lifeLoss() > 0
                    ? gameQueryService.findPermanentController(gameData, target.getId())
                    : null;

            if (permanentRemovalService.removePermanentToHand(gameData, target)) {
                String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned to owner's hand by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());
            }

            if (controllerId != null) {
                if (!gameQueryService.canPlayerLifeChange(gameData, controllerId)) {
                    gameBroadcastService.logAndBroadcast(gameData,
                            gameData.playerIdToName.get(controllerId) + "'s life total can't change.");
                } else {
                    int currentLife = gameData.getLife(controllerId);
                    gameData.playerLifeTotals.put(controllerId, currentLife - e.lifeLoss());

                    String playerName = gameData.playerIdToName.get(controllerId);
                    String lifeLog = playerName + " loses " + e.lifeLoss() + " life (" + entry.getCard().getName() + ").";
                    gameBroadcastService.logAndBroadcast(gameData, lifeLog);
                    log.info("Game {} - {} loses {} life from {}", gameData.id, playerName, e.lifeLoss(), entry.getCard().getName());
                }
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        if (e.lifeLoss() > 0) {
            gameOutcomeService.checkWinCondition(gameData);
        }
    }
}
