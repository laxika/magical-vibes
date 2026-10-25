package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersLoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandLoseLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Singe-Mind Ogre: target player reveals a card at random from their hand, then loses life equal
 * to that card's mana value. The loss follows the reveal (CR 608.2c) as a {@link PlayersLoseLifeEffect} step of its
 * own, so a Lich's Duel Mastery sacrifice that replaces it is asked about (CR 616.1) after the random reveal and
 * before any life is lost, without revealing again.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealRandomCardFromTargetPlayerHandLoseLifeEqualToManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;

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
            gameLogService.append(gameData, GameLog.text(targetName + " has no cards to reveal."));
            log.info("Game {} - {} trigger: {} has no cards to reveal", gameData.id, sourceName, targetName);
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card revealed = hand.get(randomIndex);
        gameLogService.append(gameData,
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
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(new PlayersLoseLifeEffect(Map.of(targetPlayerId, manaValue))));
        }
    }
}
