package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandLoseLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Singe-Mind Ogre: target player reveals a card at random from their hand, then loses life equal
 * to that card's mana value.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealRandomCardFromTargetPlayerHandLoseLifeEqualToManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealRandomCardFromTargetPlayerHandLoseLifeEqualToManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID targetPlayerId = entry.getTargetId();
        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();

        if (hand == null || hand.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + " has no cards to reveal."));
            log.info("Game {} - {} trigger: {} has no cards to reveal", gameData.id, sourceName, targetName);
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card revealed = hand.get(randomIndex);
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.textCardText(targetName + " reveals ", revealed, " at random."));

        cardRevealService.revealToAllPlayers(
                gameData,
                targetPlayerId,
                GameEventFact.RevealZone.HAND,
                List.of(revealed));

        int manaValue = revealed.getManaValue();
        log.info("Game {} - {} trigger: {} reveals {} (mana value {}) at random",
                gameData.id, sourceName, targetName, revealed.getName(), manaValue);

        if (manaValue > 0) {
            lifeSupport.applyLifeLoss(gameData, targetPlayerId, manaValue, sourceName);
            gameOutcomeService.checkWinCondition(gameData);
        }
    }
}
