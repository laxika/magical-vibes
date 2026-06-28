package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
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
public class LoseLifeUnlessDiscardEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeUnlessDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LoseLifeUnlessDiscardEffect) effect;

        UUID targetPlayerId = entry.getTargetId();
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        boolean hasCards = hand != null && !hand.isEmpty();

        if (!hasCards) {
            // No cards to discard — auto-apply life loss
            if (!gameQueryService.canPlayerLifeChange(gameData, targetPlayerId)) {
                gameBroadcastService.logAndBroadcast(gameData, playerName + "'s life total can't change.");
            } else {
                int currentLife = gameData.getLife(targetPlayerId);
                gameData.playerLifeTotals.put(targetPlayerId, currentLife - e.lifeLoss());
                String logEntry = playerName + " has no cards to discard. " + playerName + " loses " + e.lifeLoss() + " life.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} loses {} life (no cards to discard, {})",
                        gameData.id, playerName, e.lifeLoss(), entry.getCard().getName());
            }
            return;
        }

        // Has cards — ask the target player via the may ability system
        String prompt = "Discard a card? If you don't, you lose " + e.lifeLoss() + " life. (" + entry.getCard().getName() + ")";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), targetPlayerId, List.of(e), prompt
        ));
    
    }
}
