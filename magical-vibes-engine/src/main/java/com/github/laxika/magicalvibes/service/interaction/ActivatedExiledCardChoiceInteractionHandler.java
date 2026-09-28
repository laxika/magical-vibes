package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies an opponent's choice between the two cards exiled by an activated ability. */
@Component
@RequiredArgsConstructor
public class ActivatedExiledCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ActivatedExiledCardChoice> {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.ActivatedExiledCardChoice> handledType() {
        return PendingInteraction.ActivatedExiledCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ActivatedExiledCardChoice interaction,
                             InteractionAnswer answer) {
        if (!interaction.opponentId().equals(player.getId())) {
            throw new IllegalStateException("Not the choosing opponent");
        }
        List<UUID> selected = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (selected == null || selected.size() != 1 || !interaction.validCardIds().contains(selected.getFirst())) {
            throw new IllegalStateException("Choose one exiled card");
        }

        ExiledCardEntry chosen = findValidExiledCard(gameData, interaction.validCardIds(), selected.getFirst());
        if (chosen == null) {
            throw new IllegalStateException("Chosen card is no longer available");
        }
        ExiledCardEntry other = interaction.validCardIds().stream()
                .filter(cardId -> !cardId.equals(selected.getFirst()))
                .map(cardId -> findValidExiledCard(gameData, interaction.validCardIds(), cardId))
                .findFirst()
                .orElse(null);

        gameData.interaction.clearAwaitingInput();
        putOnBottomOfOwnersLibrary(gameData, chosen);
        if (other != null) {
            returnTappedToOwnersBattlefield(gameData, other);
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private ExiledCardEntry findValidExiledCard(GameData gameData, List<UUID> validCardIds, UUID cardId) {
        if (!validCardIds.contains(cardId)) {
            return null;
        }
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        return exiled != null && !exiled.faceDown() ? exiled : null;
    }

    private void putOnBottomOfOwnersLibrary(GameData gameData, ExiledCardEntry exiled) {
        if (!gameData.removeFromExile(exiled.card().getId())) {
            throw new IllegalStateException("Chosen card is no longer available");
        }
        gameData.playerDecks.get(exiled.ownerId()).addLast(exiled.card());
        gameLogService.append(gameData, GameLog.textCardText(
                "Puts ", exiled.card(), " on the bottom of its owner's library."));
    }

    private void returnTappedToOwnersBattlefield(GameData gameData, ExiledCardEntry exiled) {
        if (!gameData.removeFromExile(exiled.card().getId())) {
            return;
        }
        Card card = exiled.card();
        Permanent permanent = new Permanent(card);
        permanent.setEnteredFromExile(true);
        permanent.tap();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, exiled.ownerId(), permanent);
        gameLogService.append(gameData, GameLog.textCardText(
                "Returns ", card, " from exile to the battlefield tapped."));
        battlefieldEntryService.handleCreatureEnteredBattlefield(
                gameData, exiled.ownerId(), card, null, false);
    }
}
