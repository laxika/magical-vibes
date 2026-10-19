package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final LibrarySearchSupport librarySearchSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry);
    }

    private void doResolve(
            GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetId();
        if (!gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        // Find and exile all attacking creatures the target player controls
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) {
            return;
        }

        List<Permanent> attackingCreatures = battlefield.stream()
                .filter(Permanent::isAttacking)
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .toList();

        int exiledCount = 0;
        for (Permanent creature : attackingCreatures) {
            permanentRemovalService.removePermanentToExile(gameData, creature);
            gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), " is exiled."));
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, creature.getCard().getName(), entry.getCard().getName());
            exiledCount++;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        // Target player may search their library for that many basic land cards, even when that many is
        // zero (they may still search, find nothing, and shuffle).
        if (librarySearchSupport.isSearchPrevented(gameData, targetPlayerId)) return;

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (deck == null || deck.isEmpty()) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, targetPlayerId);
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            return;
        }

        List<Card> basicLands = deck.stream()
                .filter(card -> card.hasType(CardType.LAND)
                        && gameQueryService.cardHasSupertype(
                        card, CardSupertype.BASIC, gameData, targetPlayerId))
                .toList();

        if (basicLands.isEmpty() && exiledCount > 0) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(gameData, gameLogService, targetPlayerId);
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            String logMsg = playerName + " searches their library but finds no basic land cards. Library is shuffled.";
            gameLogService.append(gameData, GameLog.text(logMsg));
            return;
        }

        List<Card> candidates = exiledCount == 0 ? List.of() : new ArrayList<>(basicLands);
        String prompt = "You may search your library for up to " + exiledCount
                + " basic land card" + (exiledCount != 1 ? "s" : "")
                + " and put them onto the battlefield tapped (" + exiledCount + " remaining).";

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, targetPlayerId, LibrarySearchParams.builder(targetPlayerId, candidates)
                .remainingCount(exiledCount)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.BATTLEFIELD_TAPPED)
                .declineSkipsShuffle(exiledCount > 0)
                .build(), prompt, true, playerName + " may search their library.");

        log.info("Game {} - {} may search library for up to {} basic lands", gameData.id, playerName, exiledCount);
    }
}
