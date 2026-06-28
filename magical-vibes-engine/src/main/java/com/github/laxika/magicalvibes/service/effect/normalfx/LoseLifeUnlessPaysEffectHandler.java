package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

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
                gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            } else {
                int currentLife = gameData.getLife(targetPlayerId);
                gameData.playerLifeTotals.put(targetPlayerId, currentLife - e.lifeLoss());
                String logEntry = playerName + " can't pay {" + e.payAmount() + "}. " + playerName + " loses " + e.lifeLoss() + " life.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} loses {} life (can't pay {}, {})",
                        gameData.id, playerName, e.lifeLoss(), e.payAmount(), entry.getCard().getName());
            }
            return;
        }

        // Can pay — ask the target player via the may ability system
        String prompt = "Pay {" + e.payAmount() + "}? If you don't, you lose " + e.lifeLoss() + " life. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetPlayerId, List.of(e), prompt
        ));
    
    }
}
