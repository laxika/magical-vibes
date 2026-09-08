package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandDealDamageEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Planeswalker's Fury: target opponent reveals a random card from their hand, then takes damage
 * equal to that card's mana value.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealRandomCardFromTargetPlayerHandDealDamageEqualToManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final DamageSupport damageSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealRandomCardFromTargetPlayerHandDealDamageEqualToManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) return;

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String sourceName = entry.getCard().getName();
        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(targetName + " has no cards to reveal."));
            log.info("Game {} - {} ability: {} has no cards to reveal", gameData.id, sourceName, targetName);
            return;
        }

        Card revealed = hand.get(ThreadLocalRandom.current().nextInt(hand.size()));
        gameLogService.append(gameData,
                GameLog.textCardText(targetName + " reveals ", revealed, " at random."));
        cardRevealService.revealToAllPlayers(
                gameData,
                targetPlayerId,
                GameEventFact.RevealZone.HAND,
                List.of(revealed));

        int manaValue = revealed.getManaValue();
        log.info("Game {} - {} ability: {} reveals {} (mana value {}) at random",
                gameData.id, sourceName, targetName, revealed.getName(), manaValue);
        if (manaValue <= 0) return;

        int damage = gameQueryService.applyDamageMultiplier(gameData, manaValue, entry);
        damageSupport.dealDamageToPlayer(gameData, entry, targetPlayerId, damage);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
