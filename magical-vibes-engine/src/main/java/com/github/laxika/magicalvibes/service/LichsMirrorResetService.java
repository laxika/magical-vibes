package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithGameResetEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Lich's Mirror's loss-replacement (CR 603/104): if a player who controls a permanent with
 * {@link ReplaceControllerLossWithGameResetEffect} would lose the game, instead they shuffle their
 * hand, their graveyard, and all permanents they own into their library, then draw seven cards and
 * their life total becomes 20.
 *
 * <p>The source permanent is itself owned by that player, so it is shuffled away as part of the
 * reset — the replacement can only fire once. Poison counters are intentionally left untouched
 * (per the official ruling): a player saved from a ten-poison loss loses again at the next
 * state-based check because the Mirror is now gone.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LichsMirrorResetService {

    private static final int RESET_HAND_SIZE = 7;
    private static final int RESET_LIFE_TOTAL = 20;

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameBroadcastService gameBroadcastService;

    /**
     * If {@code losingPlayerId} controls a Lich's Mirror-style permanent, replaces their loss with
     * the game reset and returns {@code true}. Otherwise does nothing and returns {@code false}, in
     * which case the caller should proceed with the normal loss.
     */
    public boolean tryReplaceLoss(GameData gameData, UUID losingPlayerId) {
        if (losingPlayerId == null) {
            return false;
        }
        Permanent mirror = gameQueryService.findControlledPermanentWithStaticEffect(
                gameData, losingPlayerId, ReplaceControllerLossWithGameResetEffect.class);
        if (mirror == null) {
            return false;
        }

        String playerName = gameData.playerIdToName.get(losingPlayerId);
        String mirrorName = mirror.getCard().getName();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                playerName + " would lose the game — " + mirrorName + " resets the game instead."));
        log.info("Game {} - {} loss replaced by {}", gameData.id, playerName, mirrorName);

        List<Card> library = gameData.playerDecks.get(losingPlayerId);
        if (library == null) {
            library = Collections.synchronizedList(new ArrayList<>());
            gameData.playerDecks.put(losingPlayerId, library);
        }

        shuffleOwnedPermanentsAway(gameData, losingPlayerId);
        moveZoneIntoLibrary(gameData.playerGraveyards.get(losingPlayerId), library);
        moveZoneIntoLibrary(gameData.playerHands.get(losingPlayerId), library);

        LibraryShuffleHelper.shuffleLibrary(gameData, losingPlayerId);

        // A prior empty-library draw put this player in the loss set; clearing it stops the
        // subsequent state-based check from finishing the game after the reset.
        gameData.playersAttemptedDrawFromEmptyLibrary.remove(losingPlayerId);

        drawCards(gameData, losingPlayerId, library);
        gameData.playerLifeTotals.put(losingPlayerId, RESET_LIFE_TOTAL);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                playerName + " draws " + RESET_HAND_SIZE + " cards and their life total becomes "
                        + RESET_LIFE_TOTAL + "."));
        return true;
    }

    /**
     * Moves every permanent owned by {@code losingPlayerId} — across all battlefields, so stolen
     * permanents come home too — into that player's library (owner-routed by the removal service).
     * Tokens can't exist in a library (CR 111.7), so they are removed and purged instead.
     */
    private void shuffleOwnedPermanentsAway(GameData gameData, UUID losingPlayerId) {
        List<Permanent> owned = new ArrayList<>();
        gameData.forEachPermanent((controllerId, perm) -> {
            UUID ownerId = gameData.stolenCreatures.getOrDefault(perm.getId(), controllerId);
            if (ownerId.equals(losingPlayerId)) {
                owned.add(perm);
            }
        });

        for (Permanent perm : owned) {
            if (perm.getCard().isToken()) {
                permanentRemovalService.removePermanentToGraveyard(gameData, perm);
            } else {
                permanentRemovalService.removePermanentToLibraryBottom(gameData, perm);
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        // Tokens routed to the graveyard above must not end up shuffled into the library.
        List<Card> graveyard = gameData.playerGraveyards.get(losingPlayerId);
        if (graveyard != null) {
            graveyard.removeIf(Card::isToken);
        }
    }

    private void moveZoneIntoLibrary(List<Card> zone, List<Card> library) {
        if (zone == null || zone.isEmpty()) {
            return;
        }
        library.addAll(zone);
        zone.clear();
    }

    private void drawCards(GameData gameData, UUID playerId, List<Card> library) {
        int toDraw = Math.min(RESET_HAND_SIZE, library.size());
        for (int i = 0; i < toDraw; i++) {
            gameData.addCardToHand(playerId, library.removeFirst());
        }
    }
}
