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
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect) effect);
    }

    private void doResolve(
            GameData gameData, StackEntry entry,
            ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect effect) {
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
                .filter(p -> p.getCard().hasType(CardType.CREATURE))
                .toList();

        int exiledCount = 0;
        for (Permanent creature : attackingCreatures) {
            permanentRemovalService.removePermanentToExile(gameData, creature);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(creature.getCard(), " is exiled."));
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, creature.getCard().getName(), entry.getCard().getName());
            exiledCount++;
        }

        permanentRemovalService.removeOrphanedAuras(gameData);

        if (exiledCount == 0) {
            return;
        }

        // Target player may search their library for up to that many basic land cards
        if (librarySearchSupport.isSearchPrevented(gameData, targetPlayerId)) return;

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        String playerName = gameData.playerIdToName.get(targetPlayerId);

        if (deck == null || deck.isEmpty()) {
            String logMsg = playerName + " searches their library but it is empty. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        List<Card> basicLands = deck.stream()
                .filter(card -> card.hasType(CardType.LAND) && card.getSupertypes().contains(CardSupertype.BASIC))
                .toList();

        if (basicLands.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, targetPlayerId);
            String logMsg = playerName + " searches their library but finds no basic land cards. Library is shuffled.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logMsg));
            return;
        }

        String prompt = "You may search your library for up to " + exiledCount
                + " basic land card" + (exiledCount != 1 ? "s" : "")
                + " and put them onto the battlefield tapped (" + exiledCount + " remaining).";

        librarySearchSupport.sendLibrarySearchToPlayer(gameData, targetPlayerId, LibrarySearchParams.builder(targetPlayerId, new ArrayList<>(basicLands))
                .remainingCount(exiledCount)
                .canFailToFind(true)
                .destination(LibrarySearchDestination.BATTLEFIELD_TAPPED)
                .build(), prompt, true);

        log.info("Game {} - {} may search library for up to {} basic lands", gameData.id, playerName, exiledCount);
    }
}
