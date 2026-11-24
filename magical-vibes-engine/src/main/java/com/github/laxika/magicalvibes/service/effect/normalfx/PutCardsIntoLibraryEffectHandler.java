package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibraryBottomReorderRequest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService.LocatedCard;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Moves the named cards at once and records {@link StepKind#PUT_INTO_LIBRARY}, then each owner orders the ones put
 * into their library; shuffled in, each library is shuffled once and {@link StepKind#SHUFFLE_INTO_LIBRARY} recorded. Only a lone card that everybody saw is named in the log (CR 400.2, 401.4, 406.3).
 */
@Component
@RequiredArgsConstructor
public class PutCardsIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardsIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var put = (PutCardsIntoLibraryEffect) effect;
        List<LocatedCard> named = subjectResolutionService.locatedCards(gameData, entry, put, put.cards());
        StepKind kind = put.shuffled() ? StepKind.SHUFFLE_INTO_LIBRARY : StepKind.PUT_INTO_LIBRARY;
        entry.recordStep(kind, StepOutcome.NONE);
        Map<UUID, List<Card>> byOwner = new LinkedHashMap<>();
        Map<UUID, Boolean> publiclyKnown = new LinkedHashMap<>();
        List<UUID> movedIds = new ArrayList<>();
        List<UUID> revealedIds = entry.getResolutionResults().outcome(StepKind.REVEAL).objectIds();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (LocatedCard located : named) {
                Card card = located.card();
                // Revealing doesn't make a card leave its library or hand (CR 701.20b).
                boolean stillRevealed = located.zone() == Zone.LIBRARY && revealedIds.contains(card.getId());
                boolean seen = stillRevealed || located.zone() == Zone.GRAVEYARD
                        || located.zone() == Zone.EXILE && !gameData.isExiledFaceDown(card.getId());
                if (located.zone() == Zone.LIBRARY) {
                    if (!stillRevealed || !gameData.playerDecks.get(located.ownerId())
                            .removeIf(inLibrary -> inLibrary.getId().equals(card.getId()))) {
                        continue;
                    }
                } else if (located.zone() == Zone.GRAVEYARD) {
                    permanentRemovalService.removeCardFromGraveyardById(gameData, card.getId());
                } else if (located.zone() == Zone.EXILE) {
                    if (!gameData.removeFromExile(card.getId())) {
                        continue;
                    }
                } else if (located.zone() != Zone.HAND || !gameData.playerHands.get(located.ownerId())
                        .removeIf(held -> held.getId().equals(card.getId()))) {
                    continue;
                }
                byOwner.computeIfAbsent(located.ownerId(), ignored -> new ArrayList<>()).add(card);
                publiclyKnown.merge(located.ownerId(), seen, Boolean::logicalAnd);
                movedIds.add(card.getId());
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        if (put.shuffled()) {
            shuffleIn(gameData, entry, put, byOwner, publiclyKnown);
            return;
        }
        if (!movedIds.isEmpty()) {
            entry.recordStep(StepKind.PUT_INTO_LIBRARY,
                    StepOutcome.ofObjects(movedIds, List.copyOf(byOwner.keySet())));
        }
        String position = put.onTop() ? " on top of" : " on the bottom of";
        boolean asked = false;
        for (Map.Entry<UUID, List<Card>> group : byOwner.entrySet()) {
            UUID ownerId = group.getKey();
            List<Card> cards = group.getValue();
            String ownerName = gameData.playerIdToName.get(ownerId);
            if (cards.size() == 1 && publiclyKnown.get(ownerId)) {
                gameLogService.append(gameData, GameLog.textCardText(ownerName + " puts ", cards.getFirst(),
                        position + " their library."));
            } else {
                gameLogService.append(gameData, GameLog.text(ownerName + " puts " + cards.size() + " card(s)"
                        + position + " their library" + (put.randomOrder() ? " in a random order." : ".")));
            }
            if (cards.size() == 1 || put.randomOrder()) {
                Collections.shuffle(cards, ThreadLocalRandom.current());
                List<Card> library = gameData.playerDecks.get(ownerId);
                library.addAll(put.onTop() ? 0 : library.size(), cards);
                continue;
            }
            String prompt = put.onTop()
                    ? "Put these cards on top of your library in any order (first chosen will be on top)."
                    : "Put these cards on the bottom of your library in any order (first chosen will be closest to "
                    + "the top).";
            var reorder = new PendingInteraction.LibraryReorder(
                    ownerId, new ArrayList<>(cards), !put.onTop(), ownerId, prompt);
            if (!asked) {
                asked = true;
                interactionHandlerRegistry.begin(gameData, reorder);
            } else if (put.onTop()) {
                gameData.queueInteraction(reorder);
            } else {
                gameData.pendingLibraryBottomReorders.addLast(
                        new LibraryBottomReorderRequest(ownerId, new ArrayList<>(cards)));
            }
        }
    }

    /**
     * Puts each owner's cards into their library and shuffles it, then the libraries of the players the effect always
     * shuffles that got none (CR 701.24c, 701.24d), each once; records how many each owner shuffled in.
     */
    private void shuffleIn(GameData gameData, StackEntry entry, PutCardsIntoLibraryEffect put,
                           Map<UUID, List<Card>> byOwner, Map<UUID, Boolean> publiclyKnown) {
        Map<UUID, Integer> shuffledIn = new LinkedHashMap<>();
        for (Map.Entry<UUID, List<Card>> group : byOwner.entrySet()) {
            UUID ownerId = group.getKey();
            List<Card> cards = group.getValue();
            gameData.playerDecks.get(ownerId).addAll(cards);
            shuffledIn.put(ownerId, cards.size());
            LibraryShuffleHelper.shuffleLibrary(gameData, ownerId);
            String ownerName = gameData.playerIdToName.get(ownerId);
            gameLogService.append(gameData, cards.size() == 1 && publiclyKnown.get(ownerId)
                    ? GameLog.textCardText(ownerName + " shuffles ", cards.getFirst(), " into their library.")
                    : GameLog.text(ownerName + " shuffles " + cards.size() + " card(s) into their library."));
        }
        if (put.alwaysShuffled() != null) {
            for (UUID playerId : subjectResolutionService.players(gameData, entry, put, put.alwaysShuffled())) {
                if (!shuffledIn.containsKey(playerId) && gameData.playerDecks.containsKey(playerId)) {
                    LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
                    gameLogService.append(gameData, GameLog.text(
                            gameData.playerIdToName.get(playerId) + "'s library is shuffled."));
                }
            }
        }
        if (!shuffledIn.isEmpty()) {
            entry.recordStep(StepKind.SHUFFLE_INTO_LIBRARY, StepOutcome.ofAmounts(shuffledIn));
        }
    }
}
