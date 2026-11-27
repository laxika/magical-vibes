package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchParams;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
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
    private final InteractionHandlerRegistry interactionHandlerRegistry;

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

        // "That player may search their library for that many basic land cards ... then shuffle": whether to search is
        // asked first (MaySearchLibraryChoice). A player who can't search doesn't choose to, so doesn't shuffle (the
        // Leonin Arbiter and Mindlock Orb rulings); one who searches may find fewer, or none, and still shuffles.
        if (librarySearchSupport.isSearchPrevented(gameData, targetPlayerId, false)) return;

        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        if (deck == null) return;
        String playerName = gameData.playerIdToName.get(targetPlayerId);
        List<Card> candidates = exiledCount == 0 ? List.of() : deck.stream()
                .filter(card -> card.hasType(CardType.LAND)
                        && gameQueryService.cardHasSupertype(
                        card, CardSupertype.BASIC, gameData, targetPlayerId))
                .toList();
        String prompt = "Search your library for up to " + exiledCount
                + " basic land card" + (exiledCount != 1 ? "s" : "")
                + " and put them onto the battlefield tapped (" + exiledCount + " remaining).";
        gameLogService.append(gameData, GameLog.text(playerName + " may search their library."));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MaySearchLibraryChoice(targetPlayerId,
                LibrarySearchParams.builder(targetPlayerId, new ArrayList<>(candidates))
                        .remainingCount(exiledCount)
                        .canFailToFind(true)
                        .destination(LibrarySearchDestination.BATTLEFIELD_TAPPED)
                        .build(), prompt));
    }
}
