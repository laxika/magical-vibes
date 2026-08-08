package com.github.laxika.magicalvibes.service.library;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Moves a player's hand and graveyard into their library.
 *
 * <p>Timetwister-style effects and Lich's Mirror's loss replacement both do this. They had
 * hand-rolled it separately and drifted: only one of them told {@link GraveyardService} that cards
 * had left the graveyard, so after a Lich's Mirror reset every "if one or more cards left your
 * graveyard this turn" reader was stale. Sharing the move is what keeps that notification
 * impossible to forget.
 *
 * <p>Shuffling is the caller's job ({@link LibraryShuffleHelper#shuffleLibrary}): a caller that
 * drains several players shuffles once per player, while one that also tucks permanents into the
 * same library shuffles once at the end.
 */
@Component
@RequiredArgsConstructor
public class ZoneToLibraryService {

    private final GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;

    /** How many cards each zone contributed, for the caller's game log. */
    public record MovedCounts(int hand, int graveyard) {}

    /**
     * Drains {@code playerId}'s hand and graveyard into their library, firing the graveyard
     * departure notification when the graveyard actually gave up cards.
     */
    public MovedCounts moveHandAndGraveyardIntoLibrary(GameData gameData, UUID playerId) {
        List<Card> library = gameData.playerDecks.get(playerId);
        if (library == null) {
            return new MovedCounts(0, 0);
        }

        int handCount = drainInto(gameData.playerHands.get(playerId), library);
        int graveyardCount = drainInto(gameData.playerGraveyards.get(playerId), library);
        if (graveyardCount > 0) {
            graveyardService.notifyCardsLeftGraveyard(gameData, playerId);
        }
        return new MovedCounts(handCount, graveyardCount);
    }

    /**
     * Drains only {@code playerId}'s hand into their library, leaving the graveyard alone
     * (The Great Aurora shuffles the hand and the battlefield, not the graveyard).
     *
     * @return how many cards the hand contributed
     */
    public int moveHandIntoLibrary(GameData gameData, UUID playerId) {
        List<Card> library = gameData.playerDecks.get(playerId);
        if (library == null) {
            return 0;
         }
         return drainInto(gameData.playerHands.get(playerId), library);
    }

    /**
     * Moves every permanent owned by {@code playerId} — scanned across all battlefields, so
     * permanents someone else has stolen come home too — into that player's library
     * (owner-routed by the removal service). Shuffling is again the caller's job.
     *
     * <p>Tokens go to the library along with everything else: this is a battlefield-to-library
     * move, not a death, so no dies triggers fire. They are then dropped, because a token ceases
     * to exist once it leaves the battlefield (CR 111.7) and shuffling one into a library would
     * only put a card there that can never be drawn.
     *
     * @return how many permanents left the battlefield
     */
    public int moveOwnedPermanentsIntoLibrary(GameData gameData, UUID playerId) {
        List<Permanent> owned = new ArrayList<>();
        gameData.forEachPermanent((controllerId, perm) -> {
            UUID ownerId = gameData.stolenCreatures.getOrDefault(perm.getId(), controllerId);
            if (ownerId.equals(playerId)) {
                owned.add(perm);
            }
        });

        int moved = permanentRemovalService.removeAllToLibraryBottom(gameData, owned).size();

        List<Card> library = gameData.playerDecks.get(playerId);
        if (library != null) {
            library.removeIf(Card::isToken);
         }
         return moved;
    }

    private static int drainInto(List<Card> zone, List<Card> library) {
        if (zone == null || zone.isEmpty()) {
            return 0;
        }
        int count = zone.size();
        library.addAll(zone);
        zone.clear();
        return count;
    }
}
