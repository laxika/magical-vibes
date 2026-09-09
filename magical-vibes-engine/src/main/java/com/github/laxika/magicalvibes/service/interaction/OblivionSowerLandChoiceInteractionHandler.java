package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OblivionSowerLandChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.OblivionSowerLandChoice> {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.OblivionSowerLandChoice> handledType() {
        return PendingInteraction.OblivionSowerLandChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.OblivionSowerLandChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosenCardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenCardIds == null) {
            chosenCardIds = List.of();
        }
        Set<UUID> uniqueChosenCardIds = new HashSet<>(chosenCardIds);
        if (uniqueChosenCardIds.size() != chosenCardIds.size()) {
            throw new IllegalStateException("A card cannot be chosen more than once");
        }
        for (UUID id : chosenCardIds) {
            if (!interaction.validCardIds().contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
        }

        Map<UUID, ExiledCardEntry> eligibleEntries = new HashMap<>();
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry entry : gameData.exiledCards) {
                if (interaction.ownerId().equals(entry.ownerId())
                        && !entry.faceDown()
                        && entry.card().hasType(CardType.LAND)
                        && interaction.validCardIds().contains(entry.card().getId())) {
                    eligibleEntries.put(entry.card().getId(), entry);
                }
            }
        }
        if (!eligibleEntries.keySet().containsAll(chosenCardIds)) {
            throw new IllegalStateException("Chosen card is no longer available");
        }

        gameData.interaction.clearAwaitingInput();

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        for (UUID cardId : chosenCardIds) {
            ExiledCardEntry entry = eligibleEntries.get(cardId);
            Card card = entry.card();
            if (!gameData.removeFromExile(card.getId())) {
                continue;
            }

            Permanent permanent = new Permanent(card);
            permanent.setEnteredFromExile(true);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, player.getId(), permanent,
                    enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, player.getId(), card, null, false);
        }

        String playerName = gameData.playerIdToName.get(player.getId());
        gameLogService.append(gameData, GameLog.text(playerName + " puts " + chosenCardIds.size()
                + (chosenCardIds.size() == 1 ? " land" : " lands")
                + " from exile onto the battlefield (" + interaction.cardName() + ")."));
        log.info("Game {} - {} puts {} land(s) from exile onto the battlefield ({})",
                gameData.id, playerName, chosenCardIds.size(), interaction.cardName());

        inputCompletionService.publishStateAfterInput(gameData);
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
