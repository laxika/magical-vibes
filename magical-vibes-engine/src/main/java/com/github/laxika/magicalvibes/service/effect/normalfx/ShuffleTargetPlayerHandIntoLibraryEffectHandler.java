package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPlayerHandIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShuffleTargetPlayerHandIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetPlayerHandIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (hand == null || library == null) {
            return;
        }

        int handSize = hand.size();
        library.addAll(hand);
        hand.clear();
        LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.text(
                playerName + " shuffles " + handSize + " card" + (handSize == 1 ? "" : "s")
                        + " from their hand into their library."));
        log.info("Game {} - {} shuffles {} cards from hand into their library ({})",
                gameData.id, playerName, handSize, entry.getCard().getName());
    }
}
