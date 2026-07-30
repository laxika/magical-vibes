package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllHandsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileAllHandsEffect}: every card in every player's hand is exiled. There is no
 * player choice and no play permission; exiling from hand is not a discard, so no discard triggers
 * fire. Mirrors {@link ExileTargetPlayerHandEffectHandler}'s log strings, one line per player.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllHandsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllHandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                continue;
            }

            String playerName = gameData.playerIdToName.get(playerId);
            int count = hand.size();
            List<Card> toExile = new ArrayList<>(hand);
            hand.clear();
            for (Card card : toExile) {
                gameData.addToExile(playerId, card);
            }

            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s hand is exiled (" + count + " card" + (count != 1 ? "s" : "") + ")."));
            log.info("Game {} - {}'s hand ({} cards) exiled", gameData.id, playerName, count);
        }
    }
}
