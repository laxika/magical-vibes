package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllOpponentsHandsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves an effect that exiles every opponent's hand without targeting or prompting. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileAllOpponentsHandsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllOpponentsHandsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) {
                continue;
            }

            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                continue;
            }

            List<Card> toExile = new ArrayList<>(hand);
            hand.clear();
            for (Card card : toExile) {
                gameData.addToExile(playerId, card);
            }

            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s hand is exiled (" + toExile.size() + " card"
                            + (toExile.size() != 1 ? "s" : "") + ")."));
            log.info("Game {} - {}'s hand ({} cards) exiled", gameData.id, playerName, toExile.size());
        }
    }
}
