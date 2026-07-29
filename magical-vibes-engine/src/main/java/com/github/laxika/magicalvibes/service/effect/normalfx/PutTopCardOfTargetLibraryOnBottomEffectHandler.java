package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTopCardOfTargetLibraryOnBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Move the top card of the target player's library to the bottom of that library. The card is not
 * revealed by this handler — the look already happened when the may-ability prompt was built.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTopCardOfTargetLibraryOnBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTopCardOfTargetLibraryOnBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            log.info("Game {} - {}'s library is empty for {}", gameData.id, playerName, sourceName);
            return;
        }

        deck.add(deck.removeFirst());
        gameLogService.append(gameData, GameLog.text(
                playerName + " puts the top card of their library on the bottom (" + sourceName + ")."));
        log.info("Game {} - top card of {}'s library put on the bottom ({})", gameData.id, playerName, sourceName);
    }
}
