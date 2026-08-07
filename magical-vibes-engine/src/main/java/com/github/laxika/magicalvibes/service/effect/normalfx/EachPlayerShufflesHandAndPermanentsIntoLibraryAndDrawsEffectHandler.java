package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesHandAndPermanentsIntoLibraryAndDrawsEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.library.ZoneToLibraryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves The Great Aurora's "each player shuffles all cards from their hand and all permanents
 * they own into their library, then draws that many cards".
 *
 * <p>Permanents are collected by owner across every battlefield, so a stolen permanent goes home to
 * its owner's library. Tokens are moved to the library like everything else — a battlefield-to-
 * library move, not a death — and then dropped, because a token ceases to exist once it leaves the
 * battlefield (CR 111.7); per the card's ruling they still count toward the number of cards drawn.
 *
 * <p>Drawing goes through {@link DrawService}, so a player who shuffled in more than their library
 * can supply is put on the empty-library loss track rather than silently drawing fewer cards.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EachPlayerShufflesHandAndPermanentsIntoLibraryAndDrawsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ZoneToLibraryService zoneToLibraryService;
    private final PermanentRemovalService permanentRemovalService;
    private final DrawService drawService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerShufflesHandAndPermanentsIntoLibraryAndDrawsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String cardName = entry.getCard().getName();

        for (UUID playerId : gameData.orderedPlayerIds) {
            String playerName = gameData.playerIdToName.get(playerId);

            int permanentCount = shuffleOwnedPermanentsAway(gameData, playerId);
            int handCount = zoneToLibraryService.moveHandIntoLibrary(gameData, playerId);
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);

            int total = handCount + permanentCount;
            gameLogService.append(gameData, GameLog.text(playerName + " shuffles their hand ("
                    + LibraryShuffleSupport.pluralCards(handCount) + ") and " + permanentCount
                    + (permanentCount == 1 ? " permanent" : " permanents")
                    + " into their library, then draws " + LibraryShuffleSupport.pluralCards(total)
                    + " (" + cardName + ")."));
            log.info("Game {} - {} shuffles hand ({}) and permanents ({}) into library and draws {} ({})",
                    gameData.id, playerName, handCount, permanentCount, total, cardName);

            for (int i = 0; i < total; i++) {
                drawService.resolveDrawCard(gameData, playerId);
            }
        }
    }

    /**
     * Moves every permanent owned by {@code playerId} into that player's library and returns how
     * many were moved (tokens included in the count, but dropped from the library afterwards).
     */
    private int shuffleOwnedPermanentsAway(GameData gameData, UUID playerId) {
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
}
