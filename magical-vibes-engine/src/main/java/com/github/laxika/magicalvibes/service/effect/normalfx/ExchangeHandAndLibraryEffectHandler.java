package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeHandAndLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves an exchange of the controller's hand and library. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeHandAndLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeHandAndLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(playerId);
        List<Card> library = gameData.playerDecks.get(playerId);
        if (hand == null || library == null) {
            return;
        }

        List<Card> handCards = new ArrayList<>(hand);
        List<Card> libraryCards = new ArrayList<>(library);
        hand.clear();
        hand.addAll(libraryCards);
        library.clear();
        library.addAll(handCards);

        String playerName = gameData.playerIdToName.get(playerId);
        gameLogService.append(gameData, GameLog.text(playerName + " exchanges their hand and library."));
        log.info("Game {} - {} exchanges their hand ({} cards) and library ({} cards)",
                gameData.id, playerName, handCards.size(), libraryCards.size());
    }
}
