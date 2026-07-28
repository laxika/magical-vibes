package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPlayerHandOnLibraryThenSearchThatManyToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Jester's Mask. The target player's hand goes on top of their library, then the
 * controller searches that library for as many cards as were put back; each chosen card goes into
 * the target player's hand (the {@link LibrarySearchDestination#HAND} destination routes to the
 * searched library's owner when {@code targetPlayerId} is set) and the library is shuffled at the
 * end of the search loop, which is driven by
 * {@link com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PutTargetPlayerHandOnLibraryThenSearchThatManyToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetPlayerHandOnLibraryThenSearchThatManyToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) return;

        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        List<Card> hand = gameData.playerHands.get(targetPlayerId);
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        if (hand == null || deck == null) return;

        int handSize = hand.size();
        if (handSize > 0) {
            // The library is shuffled at the end of the effect, so the exact order the hand cards
            // end up in on top of the library is unobservable.
            deck.addAll(0, new ArrayList<>(hand));
            hand.clear();
            gameLogService.append(gameData, GameLog.text(
                    targetName + " puts " + handSize + " card(s) from their hand on top of their library."));
        }

        // A prevented search still leaves the hand on the library — the player finds nothing.
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) {
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            return;
        }

        int effectiveCount = Math.min(handSize, deck.size());
        if (effectiveCount <= 0) {
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " searches " + targetName + "'s library for no cards. Library is shuffled."));
            return;
        }

        String prompt = "Search " + targetName + "'s library for a card to put into their hand ("
                + effectiveCount + " remaining).";
        librarySearchSupport.sendLibrarySearchToPlayer(gameData, controllerId,
                LibrarySearchParams.builder(controllerId, new ArrayList<>(deck))
                        .targetPlayerId(targetPlayerId)
                        .remainingCount(effectiveCount)
                        .canFailToFind(false)
                        .destination(LibrarySearchDestination.HAND)
                        .build(),
                prompt, false, controllerName + " searches " + targetName + "'s library.");

        log.info("Game {} - {} searching {}'s library for {} replacement cards", gameData.id,
                controllerName, targetName, effectiveCount);
    }
}
