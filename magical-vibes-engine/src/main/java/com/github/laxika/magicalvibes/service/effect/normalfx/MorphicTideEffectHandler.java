package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MovedCard;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MorphicTideEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.library.ZoneToLibraryService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Every named player counts and shuffles in the permanents they own, then each reveals that many cards from the top of
 * their library; the revealed cards stay there, recorded as the step's {@code StepKind.REVEAL} for the steps that put
 * them onto the battlefield and the rest on the bottom.
 */
@Component
@RequiredArgsConstructor
public class MorphicTideEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;
    private final ZoneToLibraryService zoneToLibraryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MorphicTideEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MorphicTideEffect tide = (MorphicTideEffect) effect;
        List<UUID> players = subjectResolutionService.players(gameData, entry, effect, tide.players());
        Map<UUID, Integer> shuffledPermanentCounts = new LinkedHashMap<>();
        for (UUID playerId : players) {
            shuffledPermanentCounts.put(playerId,
                    zoneToLibraryService.moveOwnedPermanentsIntoLibrary(gameData, playerId));
        }
        for (UUID playerId : players) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        }

        List<UUID> revealedIds = new ArrayList<>();
        List<UUID> revealingPlayerIds = new ArrayList<>();
        List<MovedCard> whereTheyAre = new ArrayList<>();
        for (UUID playerId : players) {
            List<Card> library = gameData.playerDecks.getOrDefault(playerId, List.of());
            int revealCount = Math.min(shuffledPermanentCounts.get(playerId), library.size());
            if (revealCount == 0) {
                continue;
            }
            List<Card> revealed = List.copyOf(library.subList(0, revealCount));
            revealingPlayerIds.add(playerId);
            for (Card card : revealed) {
                revealedIds.add(card.getId());
                whereTheyAre.add(new MovedCard(card.getId(), card.getId(), Zone.LIBRARY, playerId));
            }
            logReveal(gameData, entry, playerId, revealed);
            cardRevealService.revealToAllPlayers(gameData, playerId, GameEventFact.RevealZone.LIBRARY, revealed);
        }
        entry.recordStep(StepKind.REVEAL, StepOutcome.ofObjects(revealedIds, revealingPlayerIds, whereTheyAre));
    }

    private void logReveal(GameData gameData, StackEntry entry, UUID playerId, List<Card> revealedCards) {
        GameLog.Builder builder = GameLog.builder()
                .text(gameData.playerIdToName.get(playerId) + " reveals ");
        for (int i = 0; i < revealedCards.size(); i++) {
            if (i > 0) {
                builder.text(", ");
            }
            builder.card(revealedCards.get(i));
        }
        builder.text(" from the top of their library with ").card(entry.getCard()).text(".");
        gameLogService.append(gameData, builder.build());
    }
}
