package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AttachSelectedEquipmentToCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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
        if (selectedIds.size() > 1 || !validIds.containsAll(selectedIds)
                || selectedIds.stream().anyMatch(id -> !uniqueIds.add(id))) {
            throw new IllegalStateException("Choose at most one valid card");
        }

        UUID playerId = interaction.playerId();
        Card chosen = selectedIds.isEmpty() ? null : interaction.pool().stream()
                .filter(card -> card.getId().equals(selectedIds.getFirst()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen card is no longer available"));
        boolean fromLibrary = chosen != null && interaction.libraryCardIds().contains(chosen.getId());
        boolean fromHand = chosen != null && interaction.handCardIds().contains(chosen.getId());
        boolean fromOutsideGame = chosen != null
                && interaction.outsideGameCardIds().contains(chosen.getId());
        boolean toBattlefield = interaction.destination() == LibrarySearchDestination.BATTLEFIELD;
        if (chosen != null) {
            boolean auraReturnedToGraveyard = false;
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
                    auraReturnedToGraveyard = true;
                    gameLogService.append(gameData, GameLog.textCardText(
                            gameData.playerIdToName.get(playerId) + " puts ", chosen,
                            " into their graveyard because it cannot legally enchant anything."));
                } else {
                    gameData.interaction.setPendingAuraCard(chosen);
                    gameData.interaction.setPendingAuraOwnerId(playerId);
                    gameData.interaction.clearAwaitingInput();
                    playerInputService.beginAnyTargetChoice(gameData, playerId,
                            attachTargetIds, attachPlayerIds,
                            "Choose a permanent or player for " + chosen.getName() + " to enchant.");
                    return;
                }
            }
            if (toBattlefield) {
                if (!auraReturnedToGraveyard) {
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
                }
            } else {
                gameData.addCardToHand(playerId, chosen);
            }
            String zoneName = fromLibrary ? "library"
                    : fromHand ? "hand"
                    : fromOutsideGame ? "sideboard"
                    : "graveyard";
            String destination = auraReturnedToGraveyard ? "into their graveyard"
                    : toBattlefield ? "onto the battlefield" : "into their hand";
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(playerId) + " searches their " + zoneName + ", reveals ",
                    chosen, ", and puts it " + destination + "."));
            if (interaction.librarySearchAllowed()) {
                LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, playerId);
                LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            }
        } else if (interaction.librarySearchAllowed()) {
            LibrarySearchTriggerHelper.checkOpponentSearchTriggers(gameData, gameLogService, playerId);
            if (gameData.playerDecks.get(playerId) != null) {
                LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            }
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(playerId) + " searches their library but finds no "
                            + interaction.cardLabel() + "."
                            + (gameData.playerDecks.get(playerId) == null ? "" : " Library is shuffled.")));
        }

        gameData.interaction.clearAwaitingInput();
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
