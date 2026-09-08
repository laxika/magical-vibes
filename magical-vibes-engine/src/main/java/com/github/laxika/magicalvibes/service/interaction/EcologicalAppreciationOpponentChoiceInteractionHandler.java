package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Applies the opponent's Ecological Appreciation pile choice. */
@Component
@RequiredArgsConstructor
public class EcologicalAppreciationOpponentChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.EcologicalAppreciationOpponentChoice> {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final LegendRuleService legendRuleService;

    @Override
    public Class<PendingInteraction.EcologicalAppreciationOpponentChoice> handledType() {
        return PendingInteraction.EcologicalAppreciationOpponentChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.EcologicalAppreciationOpponentChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 2) {
            throw new IllegalStateException("Must choose exactly 2 cards");
        }
        Set<UUID> chosenIds = new HashSet<>();
        for (UUID cardId : cardIds) {
            if (!interaction.validCardIds().contains(cardId)) {
                throw new IllegalStateException("Invalid card ID: " + cardId);
            }
            if (!chosenIds.add(cardId)) {
                throw new IllegalStateException("Duplicate card ID: " + cardId);
            }
        }

        UUID controllerId = interaction.controllerId();
        List<Card> chosen = interaction.cards().stream()
                .filter(card -> chosenIds.contains(card.getId()))
                .toList();
        List<Card> toBattlefield = interaction.cards().stream()
                .filter(card -> !chosenIds.contains(card.getId()))
                .toList();

        gameData.interaction.clearAwaitingInput();
        gameData.playerDecks.computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                .addAll(chosen);
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        putOnBattlefield(gameData, controllerId, toBattlefield, interaction.graveyardCardIds());
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId)
                        + " shuffles two Ecological Appreciation cards into their library and puts the rest onto the battlefield."));
        finishResolution(gameData);
    }

    private void putOnBattlefield(GameData gameData, UUID controllerId, List<Card> cards,
                                  Set<UUID> graveyardCardIds) {
        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> batch = new ArrayList<>();
        List<Permanent> permanents = new ArrayList<>();
        List<Card> placedCards = new ArrayList<>();
        boolean libraryCardBlocked = false;

        for (Card card : cards) {
            Zone sourceZone = graveyardCardIds.contains(card.getId()) ? Zone.GRAVEYARD : Zone.LIBRARY;
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, sourceZone)) {
                if (sourceZone == Zone.GRAVEYARD) {
                    gameData.playerGraveyards.computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                            .add(card);
                } else {
                    gameData.playerDecks.computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                            .add(card);
                    libraryCardBlocked = true;
                }
                gameLogService.append(gameData, GameLog.cardThen(card,
                        " can't enter the battlefield and stays in its source zone."));
                continue;
            }

            Permanent permanent = new Permanent(card);
            if (sourceZone == Zone.GRAVEYARD) {
                permanent.setEnteredFromGraveyardOwnerId(controllerId);
            }
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, controllerId, permanent, enterTappedTypes, batch);
            if (gameQueryService.findPermanentById(gameData, permanent.getId()) == null) {
                continue;
            }
            batch.add(permanent);
            permanents.add(permanent);
            placedCards.add(card);
        }

        if (libraryCardBlocked) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        }

        for (int i = 0; i < placedCards.size(); i++) {
            Card card = placedCards.get(i);
            Permanent permanent = permanents.get(i);
            if (card.hasType(CardType.CREATURE)) {
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, controllerId, card, null, false);
            }
            if (card.hasType(CardType.PLANESWALKER) && card.getLoyalty() != null) {
                permanent.setCounterCount(com.github.laxika.magicalvibes.model.CounterType.LOYALTY,
                        card.getLoyalty());
                permanent.setSummoningSick(false);
            }
        }

        if (!gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }

    private void finishResolution(GameData gameData) {
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
