package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EffectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RevealedMatchingHandCardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.RevealedMatchingHandCardChoice> {

    private final EffectResolutionService effectResolutionService;
    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.RevealedMatchingHandCardChoice> handledType() {
        return PendingInteraction.RevealedMatchingHandCardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.RevealedMatchingHandCardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.choosingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 1 || !interaction.validCardIds().contains(cardIds.getFirst())) {
            throw new IllegalStateException("Choose exactly one revealed card");
        }

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("No pending effect resolution for the revealed hand choice");
        }

        Card chosen = gameData.playerHands.getOrDefault(interaction.targetPlayerId(), List.of()).stream()
                .filter(card -> card.getId().equals(cardIds.getFirst()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen card is no longer in the target's hand"));

        gameData.playerHands.get(interaction.targetPlayerId()).remove(chosen);
        exileService.exileCard(gameData, interaction.targetPlayerId(), chosen);
        gameData.interaction.clearAwaitingInput();
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " exiles ", chosen, " from "
                        + gameData.playerIdToName.get(interaction.targetPlayerId()) + "'s hand."));

        pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                List.of(interaction.thenEffect()));
        effectResolutionService.resolveEffectsFrom(gameData, pendingEntry,
                gameData.pendingEffectResolutionIndex);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }
}
