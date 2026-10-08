package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
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
    private final CardRevealService cardRevealService;
    private final InputCompletionService inputCompletionService;
    private final com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService battlefieldEntryService;

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

        if (interaction.entryRevealAbility() != null) {
            Card chosen = gameData.playerHands.getOrDefault(interaction.targetPlayerId(), List.of()).stream()
                    .filter(card -> card.getId().equals(cardIds.getFirst()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Chosen card is no longer in your hand"));
            gameData.interaction.clearAwaitingInput();
            cardRevealService.revealMatchingHandCardsToAllPlayers(
                    gameData, interaction.targetPlayerId(), List.of(chosen));
            gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " reveals ")
                    .card(chosen).text(" — ").card(interaction.entryRevealAbility().sourceCard())
                    .text(" enters untapped.").build());
            if (interaction.entryRevealAbility().sourceCard().hasType(
                    com.github.laxika.magicalvibes.model.CardType.LAND)) {
                battlefieldEntryService.processLandETBEffects(gameData, interaction.targetPlayerId(),
                        interaction.entryRevealAbility().sourceCard());
            }
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (pendingEntry == null) {
            throw new IllegalStateException("No pending effect resolution for the revealed hand choice");
        }

        Card chosen = gameData.playerHands.getOrDefault(interaction.targetPlayerId(), List.of()).stream()
                .filter(card -> card.getId().equals(cardIds.getFirst()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen card is no longer in the target's hand"));

        gameData.interaction.clearAwaitingInput();
        pendingEntry.setChosenObjectCard(chosen);
        int previousEffectIndex = gameData.pendingEffectResolutionIndex - 1;
        if (interaction.keepInHand()
                && interaction.choosingPlayerId().equals(interaction.targetPlayerId())
                && previousEffectIndex >= 0
                && previousEffectIndex < pendingEntry.getEffectsToResolve().size()
                && pendingEntry.getEffectsToResolve().get(previousEffectIndex) instanceof MayEffect may
                && may.wrapped() instanceof MayRevealSubtypeFromHandEffect) {
            cardRevealService.revealMatchingHandCardsToAllPlayers(
                    gameData, interaction.targetPlayerId(), List.of(chosen));
        }
        if (interaction.keepInHand()) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " keeps ", chosen, " in "
                            + gameData.playerIdToName.get(interaction.targetPlayerId()) + "'s hand."));
        } else {
            gameData.playerHands.get(interaction.targetPlayerId()).remove(chosen);
            exileService.exileCard(gameData, interaction.targetPlayerId(), chosen);
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " exiles ", chosen, " from "
                            + gameData.playerIdToName.get(interaction.targetPlayerId()) + "'s hand."));
        }

        pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                List.of(interaction.thenEffect()));
        effectResolutionService.resolveEffectsFrom(gameData, pendingEntry,
                gameData.pendingEffectResolutionIndex);
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }
}
