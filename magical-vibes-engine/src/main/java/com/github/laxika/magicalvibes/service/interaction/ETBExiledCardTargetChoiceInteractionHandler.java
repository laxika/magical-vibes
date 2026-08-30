package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ETBExiledCardTargetChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ETBExiledCardTargetChoice> {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;
    private final ETBTokenTargetService etbTokenTargetService;

    @Override
    public Class<PendingInteraction.ETBExiledCardTargetChoice> handledType() {
        return PendingInteraction.ETBExiledCardTargetChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ETBExiledCardTargetChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() != 1
                || !interaction.validCardIds().contains(chosenIds.getFirst())) {
            throw new IllegalStateException("Choose one face-up exiled card");
        }

        ExiledCardEntry chosen = findFaceUpExiledCard(gameData, chosenIds.getFirst());
        if (chosen == null) {
            throw new IllegalStateException("Chosen card is no longer available");
        }

        gameData.interaction.clearAwaitingInput();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                interaction.sourceCard(),
                interaction.controllerId(),
                interaction.sourceCard().getName() + "'s ETB ability",
                new ArrayList<>(interaction.effects()),
                chosen.card().getId(),
                Zone.EXILE,
                interaction.sourcePermanentId());
        entry.setTriggeringPermanentId(interaction.triggeringPermanentId());
        if (interaction.sourcePermanentId() != null) {
            Permanent sourcePermanent = gameQueryService.findPermanentById(
                    gameData, interaction.sourcePermanentId());
            if (sourcePermanent != null) {
                entry.setSourcePermanentSnapshot(new Permanent(sourcePermanent));
            }
        }
        gameData.stack.add(entry);
        triggerCollectionService.checkBecomesTargetOfAbilityTriggers(gameData);

        gameLogService.append(gameData, GameLog.builder()
                .card(interaction.sourceCard())
                .text("'s ETB ability targets " + chosen.card().getName() + ".")
                .build());

        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
            return;
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)) {
            etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
            return;
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class)) {
            etbTokenTargetService.processNextETBSpellTargetTrigger(gameData);
            return;
        }
        if (!gameData.pendingMayAbilities.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
            return;
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private ExiledCardEntry findFaceUpExiledCard(GameData gameData, UUID cardId) {
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry entry : gameData.exiledCards) {
                if (cardId.equals(entry.card().getId()) && !entry.faceDown()) {
                    return entry;
                }
            }
        }
        return null;
    }
}
