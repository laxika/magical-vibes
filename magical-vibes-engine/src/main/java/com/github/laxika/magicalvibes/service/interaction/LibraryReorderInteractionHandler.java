package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Handles library reorder interactions ("put these cards on the top/bottom of your library in
 * any order"): prompts with the held-out cards and, on answer, validates the permutation and
 * puts the cards back, then continues any queued bottom-reorders (Warp World).
 */
@Slf4j
@Component
public class LibraryReorderInteractionHandler implements InteractionHandler<PendingInteraction.LibraryReorder> {

    private final GameLogService gameLogService;
    private final WarpWorldService warpWorldService;
    private final InputCompletionService inputCompletionService;
    private final DrawService drawService;

    @Autowired
    public LibraryReorderInteractionHandler(GameLogService gameLogService,
                                            WarpWorldService warpWorldService,
                                            InputCompletionService inputCompletionService,
                                            @Lazy DrawService drawService) {
        this.gameLogService = gameLogService;
        this.warpWorldService = warpWorldService;
        this.inputCompletionService = inputCompletionService;
        this.drawService = drawService;
    }

    public LibraryReorderInteractionHandler(GameLogService gameLogService,
                                            WarpWorldService warpWorldService,
                                            InputCompletionService inputCompletionService) {
        this(gameLogService, warpWorldService, inputCompletionService, null);
    }

    @Override
    public Class<PendingInteraction.LibraryReorder> handledType() {
        return PendingInteraction.LibraryReorder.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardOrder.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.LibraryReorder interaction,
                             InteractionAnswer answer) {
        List<Integer> cardOrder = ((InteractionAnswer.CardOrder) answer).cardOrder();
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to reorder");
        }

        List<Card> reorderCards = interaction.cards();
        int count = reorderCards.size();

        if (cardOrder.size() != count) {
            throw new IllegalStateException("Must specify order for all " + count + " cards");
        }

        // Validate that cardOrder is a permutation of 0..count-1
        Set<Integer> seen = new HashSet<>();
        for (int idx : cardOrder) {
            if (idx < 0 || idx >= count) {
                throw new IllegalStateException("Invalid card index: " + idx);
            }
            if (!seen.add(idx)) {
                throw new IllegalStateException("Duplicate card index: " + idx);
            }
        }

        // Apply the reorder: replace top N cards of deck with the reordered ones
        UUID reorderDeckOwnerId = interaction.deckOwnerId() != null ? interaction.deckOwnerId() : player.getId();
        List<Card> deck = gameData.playerDecks.get(reorderDeckOwnerId);

        if (interaction.toBottom()) {
            for (int i = 0; i < count; i++) {
                deck.add(reorderCards.get(cardOrder.get(i)));
            }
        } else {
            for (int i = 0; i < count; i++) {
                deck.add(i, reorderCards.get(cardOrder.get(i)));
            }
        }

        // Clear awaiting state
        gameData.interaction.clearAwaitingInput();
        boolean reorderedToBottom = interaction.toBottom();

        String logMsg = reorderedToBottom
                ? player.getUsername() + " puts " + count + " cards on the bottom of their library."
                : player.getUsername() + " puts " + count + " cards back on top of their library.";
        gameLogService.append(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} reordered {} {} cards", gameData.id, player.getUsername(), count,
                reorderedToBottom ? "bottom" : "top");

        for (int i = 0; i < interaction.drawAfterReorder(); i++) {
            drawService.resolveDrawCard(gameData, reorderDeckOwnerId);
        }

        if (reorderedToBottom && !gameData.pendingLibraryBottomReorders.isEmpty()) {
            warpWorldService.beginNextPendingLibraryBottomReorder(gameData);
            return;
        }
        if (reorderedToBottom && gameData.warpWorldOperation.sourceName != null) {
            warpWorldService.finalizePendingWarpWorld(gameData);
        }

        // Resumes the remaining effects on the same spell/ability (e.g. Ponder: "Look at top 3,
        // reorder, you may shuffle, then draw a card.") before auto-passing.
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
