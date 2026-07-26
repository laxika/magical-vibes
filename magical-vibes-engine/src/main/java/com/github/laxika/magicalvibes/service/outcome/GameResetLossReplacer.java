package com.github.laxika.magicalvibes.service.outcome;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ReplaceControllerLossWithGameResetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.library.ZoneToLibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Lich's Mirror's loss replacement (CR 603/104): if a player who controls a permanent with
 * {@link ReplaceControllerLossWithGameResetEffect} would lose the game, instead they shuffle their
 * hand, their graveyard, and all permanents they own into their library, then draw seven cards and
 * their life total becomes 20.
 *
 * <p>Every {@link LossReason} is replaceable here — the enum's constants are exactly the four
 * events the official ruling lists ("as a state-based action for having 0 or less life … for
 * having tried to draw a card from an empty library … for having ten or more poison counters …
 * because an ability (such as the one from Immortal Coil) states that you do so").
 *
 * <p>The source permanent is itself owned by that player, so it is shuffled away as part of the
 * reset — the replacement can only fire once. Poison counters are intentionally left untouched
 * (per the ruling): a player saved from a ten-poison loss loses again at the next state-based
 * check because the Mirror is now gone.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameResetLossReplacer implements LossReplacer {

    private static final int RESET_HAND_SIZE = 7;
    private static final int RESET_LIFE_TOTAL = 20;

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final ZoneToLibraryService zoneToLibraryService;
    private final LifeSupport lifeSupport;

    @Override
    public boolean tryReplace(GameData gameData, UUID losingPlayerId, LossReason reason) {
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
        gameLogService.append(gameData, GameLog.text(
                playerName + " would lose the game — " + mirrorName + " resets the game instead."));
        log.info("Game {} - {} loss ({}) replaced by {}", gameData.id, playerName, reason, mirrorName);

        List<Card> library = gameData.playerDecks.computeIfAbsent(
                losingPlayerId, id -> Collections.synchronizedList(new ArrayList<>()));

        shuffleOwnedPermanentsAway(gameData, losingPlayerId, library);
        zoneToLibraryService.moveHandAndGraveyardIntoLibrary(gameData, losingPlayerId);

        LibraryShuffleHelper.shuffleLibrary(gameData, losingPlayerId);

        // A prior empty-library draw put this player in the loss set; clearing it stops the
        // subsequent state-based check from finishing the game after the reset. If one of the
        // seven draws below still can't be completed, drawCards puts them straight back.
        gameData.playersAttemptedDrawFromEmptyLibrary.remove(losingPlayerId);

        int drawn = drawCards(gameData, losingPlayerId, library);

        // CR 119.5: the total *becomes* 20 by gaining or losing exactly that much life, so this
        // goes through LifeSupport rather than writing playerLifeTotals — "other cards that
        // interact with life gain or life loss will interact with this effect accordingly", and a
        // player who can't gain life keeps their old total and loses at the next check.
        lifeSupport.applySetLifeTotal(gameData, losingPlayerId, RESET_LIFE_TOTAL);

        gameLogService.append(gameData, GameLog.text(
                playerName + " draws " + drawn + (drawn == 1 ? " card" : " cards")
                        + " and their life total becomes " + gameData.getLife(losingPlayerId) + "."));
        return true;
    }

    /**
     * Moves every permanent owned by {@code losingPlayerId} — across all battlefields, so stolen
     * permanents come home too — into that player's library (owner-routed by the removal service).
     *
     * <p>Tokens go to the library along with everything else: the ruling says they are shuffled in
     * and "will leave play", which is a battlefield-to-library move, not a death, so no dies
     * triggers fire. They are then dropped, because a token ceases to exist once it leaves the
     * battlefield (CR 111.7) and "there's no point to physically shuffling tokens into your
     * library because you can't draw them".
     */
    private void shuffleOwnedPermanentsAway(GameData gameData, UUID losingPlayerId, List<Card> library) {
        List<Permanent> owned = new ArrayList<>();
        gameData.forEachPermanent((controllerId, perm) -> {
            UUID ownerId = gameData.stolenCreatures.getOrDefault(perm.getId(), controllerId);
            if (ownerId.equals(losingPlayerId)) {
                owned.add(perm);
            }
        });

        permanentRemovalService.removeAllToLibraryBottom(gameData, owned);

        library.removeIf(Card::isToken);
    }

    /**
     * Draws up to seven cards, stopping at the first draw that can't be completed.
     *
     * <p>A failed draw re-arms the empty-library loss (CR 704.5b) instead of being silently
     * skipped: per the ruling, a player whose owned permanents plus hand, graveyard and library
     * come to fewer than seven cards "will be unable to complete at least one of those draws and
     * will lose the game the next time state-based actions are checked".
     *
     * @return how many cards were actually drawn
     */
    private int drawCards(GameData gameData, UUID playerId, List<Card> library) {
        for (int drawn = 0; drawn < RESET_HAND_SIZE; drawn++) {
            if (library.isEmpty()) {
                gameData.playersAttemptedDrawFromEmptyLibrary.add(playerId);
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(playerId) + " has no cards to draw."));
                return drawn;
            }
            gameData.addCardToHand(playerId, library.removeFirst());
        }
        return RESET_HAND_SIZE;
    }
}
