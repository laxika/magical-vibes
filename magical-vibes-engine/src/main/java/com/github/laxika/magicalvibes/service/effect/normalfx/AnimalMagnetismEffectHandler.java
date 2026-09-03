package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingAnimalMagnetismChoice;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AnimalMagnetismEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/** Resolves Animal Magnetism's revealed-card opponent choice. */
@Component
@RequiredArgsConstructor
public class AnimalMagnetismEffectHandler implements NormalEffectHandlerBean {

    private static final int REVEAL_COUNT = 5;

    private final GameLogService gameLogService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final StateBasedActionService stateBasedActionService;

    @Autowired
    @Lazy
    private GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnimalMagnetismEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, REVEAL_COUNT);
        if (result == null) {
            return;
        }

        List<Card> revealedCards = result.topCards();
        logReveal(gameData, result, entry, revealedCards);

        List<UUID> creatureIds = revealedCards.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .map(Card::getId)
                .toList();
        if (creatureIds.isEmpty()) {
            putAllIntoGraveyard(gameData, result.controllerId(), revealedCards);
            return;
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(result.controllerId()))
                .toList();
        if (opponents.isEmpty()) {
            gameData.playerDecks.get(result.controllerId()).addAll(0, revealedCards);
            return;
        }

        PermanentChoiceContext.AnimalMagnetismOpponentChoice choice =
                new PermanentChoiceContext.AnimalMagnetismOpponentChoice(
                        result.controllerId(), revealedCards);
        if (opponents.size() == 1) {
            beginOpponentCardChoice(gameData, opponents.getFirst(), choice);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(choice);
        playerInputService.beginAnyTargetChoice(gameData, result.controllerId(), List.of(), opponents,
                entry.getCard().getName() + " — choose an opponent to choose a creature.");
    }

    public void completeOpponentChoice(GameData gameData, UUID opponentId,
                                       PermanentChoiceContext.AnimalMagnetismOpponentChoice choice) {
        beginOpponentCardChoice(gameData, opponentId, choice);
    }

    private void beginOpponentCardChoice(GameData gameData, UUID opponentId,
                                         PermanentChoiceContext.AnimalMagnetismOpponentChoice choice) {
        List<Card> revealedCards = choice.revealedCards();
        List<UUID> creatureIds = revealedCards.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .map(Card::getId)
                .toList();
        gameData.queueInteraction(new PendingAnimalMagnetismChoice(choice.controllerId()));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                opponentId, revealedCards, creatureIds, true, false, false, false, false,
                0, null, 1,
                "Choose a creature card to put onto the battlefield. Put the rest into its controller's graveyard.",
                false, 1, false));
    }

    public void completeCardChoice(GameData gameData, List<Card> revealedCards,
                                   List<UUID> selectedCardIds) {
        PendingAnimalMagnetismChoice pending =
                gameData.pollPendingInteraction(PendingAnimalMagnetismChoice.class);
        if (pending == null) {
            throw new IllegalStateException("No pending Animal Magnetism choice");
        }

        UUID chosenId = selectedCardIds.getFirst();
        Card chosenCard = revealedCards.stream()
                .filter(card -> card.getId().equals(chosenId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen card is not revealed"));
        EnumSet<CardType> enterTappedTypesSnapshot = EnumSet.noneOf(CardType.class);
        enterTappedTypesSnapshot.addAll(battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, pending.controllerId(), new Permanent(chosenCard, Zone.LIBRARY),
                enterTappedTypesSnapshot);
        gameLogService.append(gameData,
                GameLog.entersBattlefieldUnder(chosenCard, gameData.playerIdToName.get(pending.controllerId())));
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, pending.controllerId(), chosenCard, null, false);

        for (Card card : revealedCards) {
            if (!card.getId().equals(chosenId)) {
                graveyardService.addCardToGraveyard(gameData, pending.controllerId(), card, Zone.LIBRARY);
            }
        }

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null
                || gameData.pendingEffectResolutionIndex >= pendingEntry.getEffectsToResolve().size()) {
            stateBasedActionService.performStateBasedActions(gameData);
        }
    }

    private void putAllIntoGraveyard(GameData gameData, UUID controllerId, List<Card> cards) {
        cards.forEach(card -> graveyardService.addCardToGraveyard(gameData, controllerId, card, Zone.LIBRARY));
    }

    private void logReveal(GameData gameData, LibraryRevealSupport.TopCardsResult result,
                           StackEntry entry, List<Card> revealedCards) {
        GameLog.Builder builder = GameLog.builder().text(result.playerName() + " reveals ");
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
