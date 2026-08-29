package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMurmursFromBeyondChoice;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MurmursFromBeyondEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Resolves Murmurs from Beyond's public top-library reveal and opponent choice. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MurmursFromBeyondEffectHandler implements NormalEffectHandlerBean {

    private static final int REVEAL_COUNT = 3;

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;

    @Autowired
    @Lazy
    private GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MurmursFromBeyondEffect.class;
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

        if (revealedCards.size() == 1) {
            putChosenCardIntoGraveyard(gameData, result.controllerId(), revealedCards.getFirst());
            return;
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(result.controllerId()))
                .toList();
        if (opponents.isEmpty()) {
            gameData.playerDecks.get(result.controllerId()).addAll(0, revealedCards);
            return;
        }

        PermanentChoiceContext.MurmursFromBeyondOpponentChoice choice =
                new PermanentChoiceContext.MurmursFromBeyondOpponentChoice(
                        result.controllerId(), revealedCards);
        if (opponents.size() == 1) {
            beginOpponentCardChoice(gameData, opponents.getFirst(), choice);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(choice);
        playerInputService.beginAnyTargetChoice(gameData, result.controllerId(), List.of(), opponents,
                entry.getCard().getName() + " — choose an opponent to choose a card.");
    }

    public void completeOpponentChoice(GameData gameData, UUID opponentId,
            PermanentChoiceContext.MurmursFromBeyondOpponentChoice choice) {
        beginOpponentCardChoice(gameData, opponentId, choice);
    }

    private void beginOpponentCardChoice(GameData gameData, UUID opponentId,
            PermanentChoiceContext.MurmursFromBeyondOpponentChoice choice) {
        List<Card> revealedCards = choice.revealedCards();
        List<UUID> cardIds = revealedCards.stream().map(Card::getId).toList();
        gameData.queueInteraction(new PendingMurmursFromBeyondChoice(choice.controllerId()));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                opponentId, revealedCards, cardIds, false, false, false, false, false,
                0, null, 1,
                "Choose one of the revealed cards to put into its controller's graveyard. "
                        + "The rest go into their hand.",
                1, false));
    }

    public void completeCardChoice(GameData gameData, List<Card> revealedCards,
            List<UUID> selectedCardIds) {
        PendingMurmursFromBeyondChoice pending =
                gameData.pollPendingInteraction(PendingMurmursFromBeyondChoice.class);
        if (pending == null) {
            throw new IllegalStateException("No pending Murmurs from Beyond choice");
        }

        UUID chosenId = selectedCardIds.isEmpty()
                ? revealedCards.getFirst().getId()
                : selectedCardIds.getFirst();
        for (Card card : revealedCards) {
            if (card.getId().equals(chosenId)) {
                putChosenCardIntoGraveyard(gameData, pending.controllerId(), card);
            } else {
                gameData.addCardToHand(pending.controllerId(), card);
            }
        }

    }

    private void putChosenCardIntoGraveyard(GameData gameData, UUID controllerId, Card card) {
        graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY);
    }

}
