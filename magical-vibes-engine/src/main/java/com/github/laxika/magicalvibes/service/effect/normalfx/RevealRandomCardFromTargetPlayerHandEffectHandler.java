package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealRandomCardFromTargetPlayerHandEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealRandomCardFromTargetPlayerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (hand == null || hand.isEmpty()) {
            String logEntry = targetName + " has no cards to reveal.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} trigger: {} has no cards to reveal", gameData.id, sourceName, targetName);
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card revealed = hand.get(randomIndex);
        gameLogService.append(gameData, GameLog.textCardText(targetName + " reveals " , revealed, " at random."));

        cardRevealService.revealToAllPlayers(
                gameData,
                targetPlayerId,
                GameEventFact.RevealZone.HAND,
                List.of(revealed));

        log.info("Game {} - {} trigger: {} reveals {} at random", gameData.id, sourceName, targetName, revealed.getName());
    
    }
}
