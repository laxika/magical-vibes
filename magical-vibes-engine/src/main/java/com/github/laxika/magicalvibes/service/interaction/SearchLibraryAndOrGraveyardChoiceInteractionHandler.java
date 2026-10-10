package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AttachSelectedEquipmentToCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibrarySearchTriggerHelper;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Completes a card pick from a combined library and graveyard search pool. */
@Component
@RequiredArgsConstructor
public class SearchLibraryAndOrGraveyardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.SearchLibraryAndOrGraveyardChoice> {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final AuraAttachmentService auraAttachmentService;
    private final GraveyardService graveyardService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<PendingInteraction.SearchLibraryAndOrGraveyardChoice> handledType() {
        return PendingInteraction.SearchLibraryAndOrGraveyardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.SearchLibraryAndOrGraveyardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        List<UUID> selectedIds = chosenIds == null ? List.of() : chosenIds;
        Set<UUID> validIds = new HashSet<>(interaction.validCardIds());
        Set<UUID> uniqueIds = new HashSet<>();
        if (selectedIds.size() > interaction.maxCount() || !validIds.containsAll(selectedIds)
                || selectedIds.stream().anyMatch(id -> !uniqueIds.add(id))) {
            throw new IllegalStateException("Choose at most " + interaction.maxCount() + " valid cards");
        }

        UUID playerId = interaction.playerId();
        boolean toBattlefield = interaction.destination() == LibrarySearchDestination.BATTLEFIELD;
        List<Card> selectedCards = selectedIds.stream()
                .map(id -> interaction.pool().stream()
                        .filter(card -> card.getId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Chosen card is no longer available")))
                .toList();

        if (selectedCards.isEmpty()) {
            finishSearch(gameData, interaction, selectedCards);
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " searches their library and graveyard but finds no "
                            + interaction.cardLabel() + "."
                            + (gameData.playerDecks.get(playerId) == null ? "" : " Library is shuffled.")));
        } else if (interaction.maxCount() > 1) {
            if (toBattlefield) {
                putCardsOntoBattlefield(gameData, playerId, selectedCards, interaction);
            } else {
                putCardsIntoHand(gameData, playerId, selectedCards, interaction);
            }
            finishSearch(gameData, interaction, selectedCards);
        } else {
            boolean awaitingAuraChoice = completeSingleSelection(
                    gameData, playerId, selectedCards.getFirst(), interaction, toBattlefield);
            finishSearch(gameData, interaction, selectedCards);
            if (awaitingAuraChoice) {
                return;
            }
        }

        gameData.interaction.clearAwaitingInput();
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private boolean completeSingleSelection(GameData gameData, UUID playerId, Card chosen,
                                         PendingInteraction.SearchLibraryAndOrGraveyardChoice interaction,
                                         boolean toBattlefield) {
        boolean fromLibrary = interaction.libraryCardIds().contains(chosen.getId());
        boolean fromHand = interaction.handCardIds().contains(chosen.getId());
        boolean fromOutsideGame = interaction.outsideGameCardIds().contains(chosen.getId());
        List<Card> zone = fromLibrary
                ? gameData.playerDecks.getOrDefault(playerId, List.of())
                : fromHand
                ? gameData.playerHands.getOrDefault(playerId, List.of())
                : fromOutsideGame
                ? com.github.laxika.magicalvibes.service.OutsideGameCards.view(gameData, playerId)
                : gameData.playerGraveyards.getOrDefault(playerId, List.of());
        boolean removed = zone.removeIf(card -> card.getId().equals(chosen.getId()));
        if (!removed) {
            throw new IllegalStateException("Chosen card is no longer in its search zone");
        }
        if (!fromLibrary && !fromHand && !fromOutsideGame) {
            graveyardService.notifyCardsLeftGraveyard(gameData, playerId, chosen);
        }
        if (toBattlefield && interaction.attachAuraOrEquipment() && chosen.isAura()) {
            List<UUID> attachTargetIds = gameData.orderedPlayerIds.stream()
                    .flatMap(battlefieldPlayerId -> gameData.playerBattlefields
                            .getOrDefault(battlefieldPlayerId, List.of()).stream())
                    .filter(permanent -> auraAttachmentService.canEnchant(
                            gameData, chosen, playerId, permanent))
                    .map(Permanent::getId)
                    .toList();
            List<UUID> attachPlayerIds = chosen.isEnchantPlayer()
                    ? gameData.orderedPlayerIds.stream()
                    .filter(targetPlayerId -> auraAttachmentService.canEnchantPlayer(
                            gameData, chosen, playerId, targetPlayerId))
                    .toList()
                    : List.of();
            if (attachTargetIds.isEmpty() && attachPlayerIds.isEmpty()) {
                graveyardService.addCardToGraveyard(gameData, playerId, chosen,
                        fromHand ? Zone.HAND : Zone.GRAVEYARD);
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(playerId) + " puts ", chosen,
                        " into their graveyard because it cannot legally enchant anything."));
                return false;
            }
            gameData.interaction.setPendingAuraCard(chosen);
            gameData.interaction.setPendingAuraOwnerId(playerId);
            gameData.interaction.clearAwaitingInput();
            playerInputService.beginAnyTargetChoice(gameData, playerId,
                    attachTargetIds, attachPlayerIds,
                    "Choose a permanent or player for " + chosen.getName() + " to enchant.");
            return true;
        }
        if (toBattlefield) {
            Permanent entered = graveyardReturnSupport.putCardOntoBattlefield(
                    gameData, playerId, chosen, null, null, false, false,
                    interaction.enterWithCounterType(), interaction.enterWithCounterCount(), false);
            if (entered != null && gameData.pendingEffectResolutionEntry != null) {
                gameData.pendingEffectResolutionEntry.setChosenPermanentId(entered.getId());
            }
            if (entered != null && interaction.attachToPermanentId() != null) {
                entered.setAttachedTo(interaction.attachToPermanentId());
            }
            if (entered != null && interaction.attachAuraOrEquipment()
                    && chosen.getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                queueEquipmentAttachmentFollowUp(gameData, entered.getId());
            }
        } else {
            gameData.addCardToHand(playerId, chosen);
        }
        String zoneName = fromLibrary ? "library"
                : fromHand ? "hand"
                : fromOutsideGame ? "sideboard"
                : "graveyard";
        String destination = toBattlefield ? "onto the battlefield" : "into their hand";
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " searches their " + zoneName + ", reveals ",
                chosen, ", and puts it " + destination + "."));
        return false;
    }

    private void putCardsIntoHand(GameData gameData, UUID playerId, List<Card> cards,
                                  PendingInteraction.SearchLibraryAndOrGraveyardChoice interaction) {
        for (Card card : cards) {
            removeFromSearchZone(gameData, playerId, card, interaction);
            gameData.addCardToHand(playerId, card);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(playerId) + " searches their " + zoneName(card, interaction)
                            + ", reveals ", card, ", and puts it into their hand."));
        }
    }

    private void putCardsOntoBattlefield(GameData gameData, UUID playerId, List<Card> cards,
                                         PendingInteraction.SearchLibraryAndOrGraveyardChoice interaction) {
        Set<com.github.laxika.magicalvibes.model.CardType> enterTappedTypes =
                battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> entered = new java.util.ArrayList<>();
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (Card card : cards) {
                removeFromSearchZone(gameData, playerId, card, interaction);
                Zone origin = interaction.libraryCardIds().contains(card.getId()) ? Zone.LIBRARY : Zone.GRAVEYARD;
                Permanent permanent = new Permanent(card, origin);
                if (origin == Zone.GRAVEYARD) {
                    permanent.setEnteredFromGraveyardOwnerId(playerId);
                }
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, playerId, permanent, enterTappedTypes, List.copyOf(entered));
                entered.add(permanent);
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(playerId) + " searches their " + zoneName(card, interaction)
                                + ", reveals ", card, ", and puts it onto the battlefield."));
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (gameData.pendingEffectResolutionEntry != null && !entered.isEmpty()) {
            gameData.pendingEffectResolutionEntry.setSearchedPermanentIds(
                    entered.stream().map(Permanent::getId).toList());
            if (entered.size() == 1) {
                gameData.pendingEffectResolutionEntry.setChosenPermanentId(entered.getFirst().getId());
            }
        }
        for (Permanent permanent : entered) {
            battlefieldEntryService.processCreatureETBEffects(
                    gameData, playerId, permanent.getCard(), null, false);
        }
    }

    private void removeFromSearchZone(GameData gameData, UUID playerId, Card card,
                                      PendingInteraction.SearchLibraryAndOrGraveyardChoice interaction) {
        List<Card> zone = interaction.libraryCardIds().contains(card.getId())
                ? gameData.playerDecks.getOrDefault(playerId, List.of())
                : gameData.playerGraveyards.getOrDefault(playerId, List.of());
        if (!zone.removeIf(candidate -> candidate.getId().equals(card.getId()))) {
            throw new IllegalStateException("Chosen card is no longer in its search zone");
        }
        if (!interaction.libraryCardIds().contains(card.getId())) {
            graveyardService.notifyCardsLeftGraveyard(gameData, playerId, card);
        }
    }

    private String zoneName(Card card, PendingInteraction.SearchLibraryAndOrGraveyardChoice interaction) {
        return interaction.libraryCardIds().contains(card.getId()) ? "library" : "graveyard";
    }

    private void finishSearch(GameData gameData,
                              PendingInteraction.SearchLibraryAndOrGraveyardChoice interaction,
                              List<Card> selectedCards) {
        boolean searchedLibrary = selectedCards.isEmpty()
                || selectedCards.stream().anyMatch(card -> interaction.libraryCardIds().contains(card.getId()));
        if (interaction.librarySearchAllowed() && searchedLibrary) {
            LibrarySearchTriggerHelper.recordSearchAndQueueTriggers(
                    gameData, gameLogService, interaction.playerId());
            if (gameData.playerDecks.get(interaction.playerId()) != null) {
                LibraryShuffleHelper.shuffleLibrary(gameData, interaction.playerId());
            }
        }
    }

    private void queueEquipmentAttachmentFollowUp(GameData gameData, UUID equipmentId) {
        StackEntry sourceEntry = gameData.pendingEffectResolutionEntry;
        if (sourceEntry == null) {
            return;
        }
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceEntry.getCard(),
                sourceEntry.getControllerId(),
                sourceEntry.getCard().getName() + "'s reflexive ability",
                List.of(new MayEffect(
                        new AttachSelectedEquipmentToCreatureEffect(List.of(equipmentId)),
                        "Attach that Equipment to a creature you control?")),
                null,
                sourceEntry.getSourcePermanentId()));
    }
}
