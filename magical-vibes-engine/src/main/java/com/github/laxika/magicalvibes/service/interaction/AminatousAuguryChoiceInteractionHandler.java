package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Handles Aminatou's Augury's land choice and one-per-type free-cast choices. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AminatousAuguryChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.AminatousAuguryChoice> {

    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InputCompletionService inputCompletionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<PendingInteraction.AminatousAuguryChoice> handledType() {
        return PendingInteraction.AminatousAuguryChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    /** Starts the optional land choice, or the first available nonland type. */
    public void begin(GameData gameData, UUID playerId, List<UUID> exiledCardIds) {
        List<CardType> cardTypes = representedSpellTypes(gameData, exiledCardIds);
        List<UUID> landIds = validLandIds(gameData, exiledCardIds);
        if (!landIds.isEmpty()) {
            beginChoice(gameData, playerId, exiledCardIds, List.of(), cardTypes, landIds,
                    CardType.LAND);
            return;
        }
        beginNextType(gameData, playerId, exiledCardIds, List.of(), cardTypes);
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.AminatousAuguryChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenCardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenCardIds == null) {
            chosenCardIds = List.of();
        }
        if (chosenCardIds.size() > 1
                || new HashSet<>(chosenCardIds).size() != chosenCardIds.size()
                || !interaction.validCardIds().containsAll(chosenCardIds)) {
            throw new IllegalStateException("Choose zero or one of the offered cards");
        }

        UUID chosenCardId = chosenCardIds.isEmpty() ? null : chosenCardIds.getFirst();
        ExiledCardEntry chosenEntry = chosenCardId == null
                ? null : gameData.findExiledCard(chosenCardId);
        if (chosenCardId != null && !isStillValid(interaction, chosenEntry)) {
            throw new IllegalStateException("Chosen card is no longer available");
        }

        gameData.interaction.clearAwaitingInput();
        if (chosenCardId != null && interaction.offeredCardType() == CardType.LAND) {
            if (gameData.removeFromExile(chosenCardId)) {
                graveyardReturnSupport.putCardOntoBattlefieldFromExile(
                        gameData, player.getId(), chosenEntry.card());
            }
        }

        List<UUID> chosenSpellIds = new ArrayList<>(interaction.chosenSpellIds());
        if (chosenCardId != null && interaction.offeredCardType() != CardType.LAND) {
            chosenSpellIds.add(chosenCardId);
        }

        inputCompletionService.publishStateAfterInput(gameData);
        boolean begunNext = beginNextType(gameData, player.getId(), interaction.exiledCardIds(),
                chosenSpellIds, interaction.remainingCardTypes());
        if (begunNext) {
            return;
        }
        if (!chosenSpellIds.isEmpty()) {
            exileFreeCastQueueSupport.castChosenSpellsWithoutPaying(
                    gameData, player, chosenSpellIds);
        } else {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    private boolean beginNextType(GameData gameData, UUID playerId, List<UUID> exiledCardIds,
                                  List<UUID> chosenSpellIds, List<CardType> remainingCardTypes) {
        for (int i = 0; i < remainingCardTypes.size(); i++) {
            CardType cardType = remainingCardTypes.get(i);
            List<UUID> validCardIds = validSpellIdsForType(
                    gameData, exiledCardIds, chosenSpellIds, cardType);
            if (validCardIds.isEmpty()) {
                continue;
            }

            beginChoice(gameData, playerId, exiledCardIds, chosenSpellIds,
                    remainingCardTypes.subList(i + 1, remainingCardTypes.size()), validCardIds,
                    cardType);
            return true;
        }
        return false;
    }

    private void beginChoice(GameData gameData, UUID playerId, List<UUID> exiledCardIds,
                             List<UUID> chosenSpellIds, List<CardType> remainingCardTypes,
                             List<UUID> validCardIds, CardType offeredCardType) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.AminatousAuguryChoice(
                playerId, exiledCardIds, chosenSpellIds, remainingCardTypes, validCardIds,
                offeredCardType));
        log.info("Game {} - {} awaits Aminatou's Augury {} choice among {} cards",
                gameData.id, gameData.playerIdToName.get(playerId),
                offeredCardType.getDisplayName(), validCardIds.size());
    }

    private static List<CardType> representedSpellTypes(GameData gameData, List<UUID> exiledCardIds) {
        return Arrays.stream(CardType.values())
                .filter(type -> type != CardType.LAND && !type.isPlanar())
                .filter(type -> exiledCardIds.stream()
                        .map(gameData::findExiledCard)
                        .filter(entry -> entry != null && !entry.faceDown())
                        .map(ExiledCardEntry::card)
                        .anyMatch(card -> isCastableSpell(card) && card.hasType(type)))
                .toList();
    }

    private static List<UUID> validLandIds(GameData gameData, List<UUID> exiledCardIds) {
        return exiledCardIds.stream()
                .map(gameData::findExiledCard)
                .filter(entry -> entry != null && !entry.faceDown() && entry.card().hasType(CardType.LAND))
                .map(entry -> entry.card().getId())
                .toList();
    }

    private static List<UUID> validSpellIdsForType(GameData gameData, List<UUID> exiledCardIds,
                                                   List<UUID> chosenSpellIds, CardType cardType) {
        Set<UUID> chosen = Set.copyOf(chosenSpellIds);
        return exiledCardIds.stream()
                .filter(id -> !chosen.contains(id))
                .map(gameData::findExiledCard)
                .filter(entry -> entry != null && !entry.faceDown())
                .map(ExiledCardEntry::card)
                .filter(card -> isCastableSpell(card) && card.hasType(cardType))
                .map(Card::getId)
                .toList();
    }

    private static boolean isStillValid(PendingInteraction.AminatousAuguryChoice interaction,
                                        ExiledCardEntry entry) {
        if (entry == null || entry.faceDown()) {
            return false;
        }
        Card card = entry.card();
        return interaction.offeredCardType() == CardType.LAND
                ? card.hasType(CardType.LAND)
                : isCastableSpell(card) && card.hasType(interaction.offeredCardType());
    }

    private static boolean isCastableSpell(Card card) {
        if (card.hasType(CardType.LAND)) {
            return false;
        }
        return card.hasType(CardType.INSTANT)
                || card.hasType(CardType.SORCERY)
                || (card.getType() != null && card.getType().isPermanentType());
    }
}
