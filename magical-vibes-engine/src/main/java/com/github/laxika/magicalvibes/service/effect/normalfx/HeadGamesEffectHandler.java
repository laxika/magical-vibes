package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HeadGamesEffect;
import java.util.ArrayList;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HeadGamesEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Autowired
    @Lazy
    private InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HeadGamesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, ((HeadGamesEffect) effect).cardsToFind());
    }

    private void doResolve(GameData gameData, StackEntry entry, int cardsToFind) {
        UUID casterId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        List<Card> targetDeck = gameData.playerDecks.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(casterId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        // Check search restriction — if unpaid Arbiters exist, search is prevented.
        if (!librarySearchSupport.checkSearchRestriction(gameData, casterId, targetPlayerId, casterId)) {
            // Search prevented — still execute remaining spell steps per rules:
            // target puts hand on top of library, then library is shuffled.
            librarySearchSupport.putHandOnTopOfLibrary(gameData, targetHand, targetDeck, targetName);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            String shuffleLog = targetName + "'s library is shuffled.";
            gameLogService.append(gameData, GameLog.text(shuffleLog));
            return;
        }

        int handSize = cardsToFind > 0 ? cardsToFind : targetHand.size();

        // Step 1: Target opponent puts hand on top of library
        if (handSize == 0) {
            String logMsg = targetName + " has no cards in hand. " + targetName + "'s library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            return;
        }

        if (cardsToFind > 0) {
            log.info("Game {} - Head Games continues after {} ordered their hand",
                    gameData.id, targetName);
        } else if (handSize > 1 && handSize + targetDeck.size()
                > librarySearchSupport.opponentSearchTopCardsLimit(gameData, casterId)) {
            // The caster can only search the top of the library, so the order the opponent puts
            // their hand on top matters (CR 401.4): they order it, then the search follows.
            List<Card> toOrder = new ArrayList<>(targetHand);
            targetHand.clear();
            gameLogService.append(gameData, GameLog.text(targetName + " puts " + handSize
                    + " cards from their hand on top of their library."));
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(new HeadGamesEffect(handSize)));
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                    targetPlayerId, toOrder, false, targetPlayerId,
                    "Put your hand on top of your library in any order."));
            return;
        } else {
            librarySearchSupport.putHandOnTopOfLibrary(gameData, targetHand, targetDeck, targetName);
        }

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
