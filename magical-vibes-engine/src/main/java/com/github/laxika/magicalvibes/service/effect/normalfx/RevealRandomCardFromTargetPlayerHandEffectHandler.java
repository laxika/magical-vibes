package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.RevealHandMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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

    private final CardViewFactory cardViewFactory;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final SessionManager sessionManager;

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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} trigger: {} has no cards to reveal", gameData.id, sourceName, targetName);
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card revealed = hand.get(randomIndex);
        String logEntry = targetName + " reveals " + revealed.getName() + " at random.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        List<CardView> cardViews = List.of(cardViewFactory.create(revealed));
        for (UUID playerId : gameData.orderedPlayerIds) {
            sessionManager.sendToPlayer(playerId, new RevealHandMessage(cardViews, targetName));
        }

        log.info("Game {} - {} trigger: {} reveals {} at random", gameData.id, sourceName, targetName, revealed.getName());
    
    }
}
