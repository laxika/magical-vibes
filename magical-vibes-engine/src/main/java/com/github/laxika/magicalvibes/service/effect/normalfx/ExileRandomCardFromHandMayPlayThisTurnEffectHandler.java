package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ExileToOwnerGraveyardAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromHandMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Elkin Lair: the target player (active player from {@code EACH_UPKEEP_TRIGGERED}) exiles a random
 * hand card, may play it this turn, and unplayed cards go to the graveyard at the next end step.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileRandomCardFromHandMayPlayThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomCardFromHandMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (hand == null || hand.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " has no cards in hand — nothing to exile."));
            return;
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(hand.size());
        Card exiled = hand.remove(randomIndex);
        exileService.exileCard(gameData, playerId, exiled);

        gameData.exilePlayPermissions.put(exiled.getId(), playerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(exiled.getId());
        gameData.queueDelayedAction(new ExileToOwnerGraveyardAtNextEndStep(
                exiled.getId(), playerId, entry.getCard()));

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles ").card(exiled)
                .text(" at random from their hand (may play it this turn; unplayed → graveyard "
                        + "at next end step).").build());
        log.info("Game {} - {} exiles {} at random from hand (Elkin Lair)",
                gameData.id, playerName, exiled.getName());
    }
}
