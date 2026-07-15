package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsMessage;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.WarpWorldService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
@RequiredArgsConstructor
public class LibraryReorderInteractionHandler implements InteractionHandler<PendingInteraction.LibraryReorder> {

    private final SessionManager sessionManager;
    private final CardViewFactory cardViewFactory;
    private final GameBroadcastService gameBroadcastService;
    private final WarpWorldService warpWorldService;
    private final PlayerInputService playerInputService;
    private final TurnProgressionService turnProgressionService;
    private final EffectResolutionService effectResolutionService;

    @Override
    public Class<PendingInteraction.LibraryReorder> handledType() {
        return PendingInteraction.LibraryReorder.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardOrder.class;
    }

    @Override
    public UUID decidingPlayerId(PendingInteraction.LibraryReorder interaction) {
        return interaction.playerId();
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.LibraryReorder interaction, UUID recipientId) {
        List<CardView> cardViews = interaction.cards().stream().map(cardViewFactory::create).toList();
        sessionManager.sendToPlayer(recipientId, new ReorderLibraryCardsMessage(cardViews, interaction.prompt()));
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
        log.info("Game {} - {} reordered {} {} cards", gameData.id, player.getUsername(), count,
                reorderedToBottom ? "bottom" : "top");

        if (reorderedToBottom && !gameData.pendingLibraryBottomReorders.isEmpty()) {
            warpWorldService.beginNextPendingLibraryBottomReorder(gameData);
            return;
        }
        if (reorderedToBottom && gameData.warpWorldOperation.sourceName != null) {
            warpWorldService.finalizePendingWarpWorld(gameData);
        }

        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        // Resume resolving remaining effects on the same spell/ability
        // (e.g. Ponder: "Look at top 3, reorder, you may shuffle, then draw a card.")
        if (gameData.pendingEffectResolutionEntry != null) {
            effectResolutionService.resolveEffectsFrom(gameData,
                    gameData.pendingEffectResolutionEntry,
                    gameData.pendingEffectResolutionIndex);
        }

        if (!gameData.interaction.isAwaitingInput() && !gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }

        turnProgressionService.resolveAutoPass(gameData);
    }
}
