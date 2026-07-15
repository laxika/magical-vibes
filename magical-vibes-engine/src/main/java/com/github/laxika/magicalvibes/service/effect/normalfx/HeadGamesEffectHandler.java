package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HeadGamesEffect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeadGamesEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HeadGamesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (HeadGamesEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry, HeadGamesEffect effect) {
        UUID casterId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        List<Card> targetDeck = gameData.playerDecks.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        // Check search restriction — if unpaid Arbiters exist, search is prevented.
        if (!librarySearchSupport.checkSearchRestriction(gameData, casterId)) {
            // Search prevented — still execute remaining spell steps per rules:
            // target puts hand on top of library, then library is shuffled.
            librarySearchSupport.putHandOnTopOfLibrary(gameData, targetHand, targetDeck, targetName);
            Collections.shuffle(targetDeck);
            String shuffleLog = targetName + "'s library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(shuffleLog));
            return;
        }

        int handSize = targetHand.size();

        // Step 1: Target opponent puts hand on top of library
        if (handSize == 0) {
            String logMsg = targetName + " has no cards in hand. " + targetName + "'s library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            Collections.shuffle(targetDeck);
            return;
        }

        librarySearchSupport.putHandOnTopOfLibrary(gameData, targetHand, targetDeck, targetName);

        // Step 2: Caster searches target's library for that many cards
        List<Card> allCards = new ArrayList<>(targetDeck);

        String prompt = "Search " + targetName + "'s library for a card to put into their hand (" + handSize + " remaining).";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, casterId, LibrarySearchParams.builder(casterId, allCards)
                .targetPlayerId(targetPlayerId)
                .remainingCount(handSize)
                .build(), prompt, false, casterName + " searches " + targetName + "'s library.");
        log.info("Game {} - {} resolving Head Games, searching {}'s library for {} cards",
                gameData.id, casterName, targetName, handSize);
    }
}
