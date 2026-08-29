package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoseLifeUnlessPaysEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeUnlessPaysEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LoseLifeUnlessPaysEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        com.github.laxika.magicalvibes.model.ManaCost cost = new com.github.laxika.magicalvibes.model.ManaCost("{" + e.payAmount() + "}");
        com.github.laxika.magicalvibes.model.ManaPool pool = gameData.playerManaPools.get(targetPlayerId);
        boolean canPay = cost.canPay(pool);

        if (!canPay) {
            // Can't pay — auto-apply life loss
            if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
                gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
            } else {
                int lifeLoss = e.lifeLoss()
                        * gameQueryService.opponentLifeLossMultiplier(gameData, targetPlayerId);
                int currentLife = gameData.getLife(targetPlayerId);
                gameData.playerLifeTotals.put(targetPlayerId, currentLife - lifeLoss);
                String logEntry = playerName + " can't pay {" + e.payAmount() + "}. " + playerName + " loses " + lifeLoss + " life.";
                gameLogService.append(gameData, GameLog.text(logEntry));
                log.info("Game {} - {} loses {} life (can't pay {}, {})",
                        gameData.id, playerName, lifeLoss, e.payAmount(), entry.getCard().getName());
            }
            applyLifeGainIfConfigured(gameData, entry, e);
            return;
        }

        // Can pay — ask the target player via the may ability system
        String prompt = "Pay {" + e.payAmount() + "}? If you don't, you lose " + e.lifeLoss() + " life. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetPlayerId, List.of(e), prompt,
                null, null, null, null, 0, 0, null, null, null, null,
                entry.getControllerId(), null, 0
        ));
    }

    private void applyLifeGainIfConfigured(GameData gameData, StackEntry entry,
                                           LoseLifeUnlessPaysEffect effect) {
        if (effect.controllerGainsLifeLost()) {
            lifeSupport.applyGainLife(gameData, entry.getControllerId(), effect.lifeLoss());
        }
    }
}
