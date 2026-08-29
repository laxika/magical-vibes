package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Applies Verdant Mastery's opponent and controller land-distribution choices. */
@Component
@RequiredArgsConstructor
public class VerdantMasteryLandChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.VerdantMasteryLandChoice> {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.VerdantMasteryLandChoice> handledType() {
        return PendingInteraction.VerdantMasteryLandChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.VerdantMasteryLandChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null) {
            cardIds = List.of();
        }
        int required = interaction.chooseForOpponent() ? 1 : Math.min(2, interaction.cards().size());
        if (cardIds.size() != required) {
            throw new IllegalStateException("Must choose exactly " + required + " card(s)");
        }

        Set<UUID> selectedIds = new HashSet<>();
        for (UUID cardId : cardIds) {
            if (!interaction.validCardIds().contains(cardId)) {
                throw new IllegalStateException("Invalid card ID: " + cardId);
            }
            if (!selectedIds.add(cardId)) {
                throw new IllegalStateException("Duplicate card ID: " + cardId);
            }
        }

        List<Card> selected = interaction.cards().stream()
                .filter(card -> selectedIds.contains(card.getId()))
                .toList();
        List<Card> remaining = interaction.cards().stream()
                .filter(card -> !selectedIds.contains(card.getId()))
                .toList();
        gameData.interaction.clearAwaitingInput();

        if (interaction.chooseForOpponent()) {
            if (remaining.size() > 2) {
                beginControllerChoice(gameData, interaction.playerId(), interaction.opponentId(),
                        selected.getFirst(), remaining);
            } else {
                resolveDistribution(gameData, interaction.playerId(), interaction.opponentId(),
                        selected.getFirst(), remaining, List.of());
            }
            return;
        }

        resolveDistribution(gameData, interaction.playerId(), interaction.opponentId(),
                interaction.opponentCard(), selected, remaining);
    }

    void beginOpponentChoice(GameData gameData, UUID controllerId, UUID opponentId, List<Card> cards) {
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.VerdantMasteryLandChoice(
                        controllerId, opponentId, null, cards, true));
    }

    void beginControllerChoice(GameData gameData, UUID controllerId, UUID opponentId,
                               Card opponentCard, List<Card> cards) {
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.VerdantMasteryLandChoice(
                        controllerId, opponentId, opponentCard, cards, false));
    }

    void resolveDistribution(GameData gameData, UUID controllerId, UUID opponentId,
                             Card opponentCard, List<Card> ownBattlefield, List<Card> handCards) {
        putOntoBattlefield(gameData, opponentId, opponentCard);
        putOntoBattlefield(gameData, controllerId, ownBattlefield);
        gameData.playerHands.get(controllerId).addAll(handCards);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId)
                        + " puts Verdant Mastery's chosen basic lands onto the battlefield and into their hand."
                        + " Library is shuffled."));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private void putOntoBattlefield(GameData gameData, UUID controllerId, Card card) {
        if (controllerId == null || card == null) {
            return;
        }
        putOntoBattlefield(gameData, controllerId, List.of(card));
    }

    private void putOntoBattlefield(GameData gameData, UUID controllerId, List<Card> cards) {
        if (controllerId == null || cards.isEmpty()) {
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> batch = new ArrayList<>();
        for (Card card : cards) {
            Permanent permanent = new Permanent(card);
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, batch);
            permanent.tap();
            batch.add(permanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(
                    card, gameData.playerIdToName.get(controllerId)));
            if (card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                permanent.setCounterCount(CounterType.LOYALTY, card.getLoyalty());
                permanent.setSummoningSick(false);
            }
        }
    }
}
