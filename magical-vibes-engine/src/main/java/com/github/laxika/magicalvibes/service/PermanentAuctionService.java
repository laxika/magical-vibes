package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Runs a permanent auction (e.g. Thieves' Auction): exile every nontoken permanent into a shared
 * pool, then (starting with the controller, in turn order and wrapping) have each player choose one
 * exiled card and put it onto the battlefield tapped under their control, repeating until the pool
 * empties. The auction state travels on the {@link PendingInteraction.PermanentAuctionChoice}
 * record; each answered pick begins a fresh record for the next chooser.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermanentAuctionService {

    private static final String PROMPT =
            "Choose one of the auctioned cards to put onto the battlefield tapped under your control.";

    private final PermanentRemovalService permanentRemovalService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameBroadcastService gameBroadcastService;
    private final TurnProgressionService turnProgressionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    /** Exiles all nontoken permanents and begins the auction with the controller. */
    public void beginAuction(GameData gameData, UUID controllerId, String sourceName) {
        List<Permanent> toExile = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent perm : battlefield) {
                if (!perm.getCard().isToken()) {
                    toExile.add(perm);
                }
            }
        });

        List<Card> pool = new ArrayList<>();
        for (Permanent perm : toExile) {
            Card card = perm.getOriginalCard();
            permanentRemovalService.removePermanentToExile(gameData, perm);
            pool.add(card);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(card.getName() + " is exiled."));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (pool.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(sourceName + " exiles no permanents."));
            turnProgressionService.resolveAutoPass(gameData);
            return;
        }

        // Turn order starting with the controller.
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int start = ordered.indexOf(controllerId);
        List<UUID> playerOrder = new ArrayList<>();
        for (int i = 0; i < ordered.size(); i++) {
            playerOrder.add(ordered.get((start + i) % ordered.size()));
        }

        beginPick(gameData, controllerId, pool, playerOrder, new ArrayList<>());
    }

    /** Applies one player's auction pick and advances the auction (or finishes it). */
    public void applyPick(GameData gameData, Player player, List<UUID> cardIds) {
        PendingInteraction.PermanentAuctionChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentAuctionChoice.class);
        if (choice == null || !player.getId().equals(choice.choosingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<Card> pool = new ArrayList<>(choice.pool());
        UUID chosenId = cardIds == null ? null
                : cardIds.stream()
                    .filter(id -> pool.stream().anyMatch(c -> c.getId().equals(id)))
                    .findFirst().orElse(null);
        if (chosenId == null) {
            // Mandatory pick — an empty/invalid selection re-prompts the same player.
            log.warn("Game {} - {} sent an invalid auction pick, re-prompting", gameData.id, player.getUsername());
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.PermanentAuctionChoice(
                    choice.choosingPlayerId(), pool, choice.playerOrder(), choice.placed(), PROMPT));
            return;
        }

        Card chosen = pool.stream().filter(c -> c.getId().equals(chosenId)).findFirst().orElseThrow();
        pool.removeIf(c -> c.getId().equals(chosenId));
        UUID chooserId = player.getId();

        gameData.removeFromExile(chosenId);
        Permanent permanent = new Permanent(chosen);
        permanent.tap();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, chooserId, permanent);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(player.getUsername() + " puts " + chosen.getName() + " onto the battlefield tapped."));

        List<PendingInteraction.PermanentAuctionPlacement> placed = new ArrayList<>(choice.placed());
        placed.add(new PendingInteraction.PermanentAuctionPlacement(chooserId, chosen));

        gameData.interaction.clearAwaitingInput();

        if (pool.isEmpty()) {
            finishAuction(gameData, placed);
            return;
        }

        // Advance to the next player in the rotation.
        List<UUID> order = choice.playerOrder();
        int idx = order.indexOf(chooserId);
        UUID next = order.get((idx + 1) % order.size());
        beginPick(gameData, next, pool, order, placed);
    }

    private void beginPick(GameData gameData, UUID chooserId, List<Card> pool, List<UUID> playerOrder,
                           List<PendingInteraction.PermanentAuctionPlacement> placed) {
        gameBroadcastService.broadcastGameState(gameData);
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.PermanentAuctionChoice(chooserId, pool, playerOrder, placed, PROMPT));
    }

    private void finishAuction(GameData gameData, List<PendingInteraction.PermanentAuctionPlacement> placed) {
        // Enter-the-battlefield abilities fire after every card has been chosen (all entered as part
        // of this resolution). Stop early if an ETB begins its own interaction.
        for (PendingInteraction.PermanentAuctionPlacement placement : placed) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, placement.controllerId(), placement.card(), null, false);
            if (gameData.interaction.isAwaitingInput()) {
                return;
            }
        }
        turnProgressionService.resolveAutoPass(gameData);
    }
}
