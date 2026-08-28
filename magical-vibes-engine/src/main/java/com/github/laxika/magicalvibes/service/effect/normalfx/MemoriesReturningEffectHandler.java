package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMemoriesReturningChoice;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MemoriesReturningEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Memories Returning's four alternating picks from a public top-library reveal. */
@Component
@RequiredArgsConstructor
public class MemoriesReturningEffectHandler implements NormalEffectHandlerBean {

    private static final int REVEAL_COUNT = 5;
    private static final int MAX_PICK_PHASE = 4;

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MemoriesReturningEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, REVEAL_COUNT);
        if (result == null) {
            return;
        }

        List<Card> revealedCards = result.topCards();
        GameLog.Builder revealBuilder = GameLog.builder().text(result.playerName() + " reveals ");
        for (int i = 0; i < revealedCards.size(); i++) {
            if (i > 0) {
                revealBuilder.text(", ");
            }
            revealBuilder.card(revealedCards.get(i));
        }
        revealBuilder.text(" from the top of their library with ")
                .card(entry.getCard())
                .text(".");
        gameLogService.append(gameData, revealBuilder.build());

        beginCardChoice(gameData, result.controllerId(), null, revealedCards, 0,
                entry.getCard().getName());
    }

    /** Continues the flow after the controller chooses which opponent makes the bottom pick. */
    public void completeOpponentChoice(GameData gameData, UUID opponentId,
            PermanentChoiceContext.MemoriesReturningOpponentChoice choice) {
        beginCardChoice(gameData, choice.controllerId(), opponentId, choice.remainingCards(),
                choice.phase(), choice.sourceCardName());
    }

    /** Applies one revealed-card pick and starts the next alternating choice. */
    public void completeCardChoice(GameData gameData, List<Card> revealedCards,
            List<UUID> selectedCardIds) {
        PendingMemoriesReturningChoice pending =
                gameData.pollPendingInteraction(PendingMemoriesReturningChoice.class);
        if (pending == null) {
            throw new IllegalStateException("No pending Memories Returning choice");
        }

        UUID selectedId = selectedCardIds.isEmpty()
                ? revealedCards.getFirst().getId()
                : selectedCardIds.getFirst();
        Card selected = revealedCards.stream()
                .filter(card -> card.getId().equals(selectedId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Selected card is not revealed"));

        if ((pending.phase() & 1) == 0) {
            gameData.addCardToHand(pending.controllerId(), selected);
        } else {
            gameData.playerDecks.get(pending.controllerId()).add(selected);
        }

        List<Card> remainingCards = new ArrayList<>(revealedCards);
        remainingCards.remove(selected);
        int nextPhase = pending.phase() + 1;
        if (remainingCards.isEmpty() || nextPhase >= MAX_PICK_PHASE) {
            for (Card card : remainingCards) {
                gameData.addCardToHand(pending.controllerId(), card);
            }
            return;
        }

        if ((nextPhase & 1) == 0) {
            beginCardChoice(gameData, pending.controllerId(), pending.opponentId(), remainingCards,
                    nextPhase, pending.sourceCardName());
        } else if (pending.opponentId() != null) {
            beginCardChoice(gameData, pending.controllerId(), pending.opponentId(), remainingCards,
                    nextPhase, pending.sourceCardName());
        } else {
            beginOpponentChoice(gameData, pending.controllerId(), remainingCards, nextPhase,
                    pending.sourceCardName());
        }
    }

    private void beginCardChoice(GameData gameData, UUID controllerId, UUID opponentId,
            List<Card> cards, int phase, String sourceCardName) {
        if (cards.isEmpty()) {
            return;
        }
        UUID picker = (phase & 1) == 0 ? controllerId : opponentId;
        if (picker == null) {
            beginOpponentChoice(gameData, controllerId, cards, phase, sourceCardName);
            return;
        }

        gameData.queueInteraction(new PendingMemoriesReturningChoice(
                controllerId, opponentId, phase, sourceCardName));
        String prompt = (phase & 1) == 0
                ? "Put one of these revealed cards into your hand."
                : "Put one of these revealed cards on the bottom of the library.";
        List<UUID> cardIds = cards.stream().map(Card::getId).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                picker, cards, cardIds, false, false, false, false, false,
                0, null, 1, prompt, false, 1, false));
    }

    private void beginOpponentChoice(GameData gameData, UUID controllerId, List<Card> cards,
            int phase, String sourceCardName) {
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
        if (opponents.isEmpty()) {
            for (Card card : cards) {
                gameData.addCardToHand(controllerId, card);
            }
            return;
        }

        if (opponents.size() == 1) {
            beginCardChoice(gameData, controllerId, opponents.getFirst(), cards, phase, sourceCardName);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.MemoriesReturningOpponentChoice(
                        controllerId, cards, phase, sourceCardName));
        playerInputService.beginPlayerChoice(gameData, controllerId, opponents,
                sourceCardName + " — choose an opponent to choose a card.");
    }
}
