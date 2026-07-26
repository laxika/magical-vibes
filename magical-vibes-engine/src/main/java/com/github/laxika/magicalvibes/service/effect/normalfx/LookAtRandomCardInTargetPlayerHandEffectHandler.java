package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtRandomCardInTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
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
public class LookAtRandomCardInTargetPlayerHandEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtRandomCardInTargetPlayerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(entry.getControllerId());
        String sourceName = entry.getCard().getName();

        if (hand == null || hand.isEmpty()) {
            String logEntry = casterName + " looks at " + targetName + "'s hand at random. It is empty.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {}: {} has no cards to look at", gameData.id, sourceName, targetName);
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card looked = hand.get(randomIndex);

        // Public log reveals only that a look happened, not the card's identity.
        String logEntry = casterName + " looks at a card at random in " + targetName + "'s hand.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

        // The chosen card is shown only to the controller.
        cardRevealService.revealToPlayer(
                gameData,
                targetPlayerId,
                GameEventFact.RevealZone.HAND,
                List.of(looked),
                entry.getControllerId());

        log.info("Game {} - {}: {} looks at a random card in {}'s hand", gameData.id, sourceName, casterName, targetName);

    }
}
