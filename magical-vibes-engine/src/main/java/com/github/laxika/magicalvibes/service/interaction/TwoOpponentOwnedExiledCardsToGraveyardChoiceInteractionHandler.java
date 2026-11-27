package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffectHandler;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwoOpponentOwnedExiledCardsToGraveyardChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.TwoOpponentOwnedExiledCardsToGraveyardChoice> {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.TwoOpponentOwnedExiledCardsToGraveyardChoice> handledType() {
        return PendingInteraction.TwoOpponentOwnedExiledCardsToGraveyardChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.TwoOpponentOwnedExiledCardsToGraveyardChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice");
        }

        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null) {
            chosenIds = List.of();
        }
        if ((!chosenIds.isEmpty() && chosenIds.size() != 2)
                || new HashSet<>(chosenIds).size() != chosenIds.size()
                || chosenIds.stream().anyMatch(id -> !interaction.validCardIds().contains(id))) {
            throw new IllegalStateException("Choose exactly two cards an opponent owns from exile, or decline");
        }

        List<ExiledCardEntry> selected = new ArrayList<>();
        synchronized (gameData.exiledCards) {
            for (UUID chosenId : chosenIds) {
                ExiledCardEntry exiled = gameData.exiledCards.stream()
                        .filter(candidate -> chosenId.equals(candidate.card().getId())
                                && gameData.playerIds.contains(candidate.ownerId())
                                && !player.getId().equals(candidate.ownerId()))
                        .findFirst().orElse(null);
                if (exiled == null) {
                    throw new IllegalStateException("Chosen card is no longer available");
                }
                selected.add(exiled);
            }
            selected = withFaceDownPicksAtRandom(gameData, selected);
        }

        gameData.interaction.clearAwaitingInput();
        if (selected.size() == 2) {
            for (ExiledCardEntry exiled : selected) {
                if (!gameData.removeFromExile(exiled.card().getId())) {
                    throw new IllegalStateException("Chosen card is no longer available");
                }
                graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
                gameLogService.append(gameData, GameLog.textCardText(
                        gameData.playerIdToName.get(exiled.ownerId()) + " puts ", exiled.card(),
                        " from exile into their graveyard."));
            }
            if (gameData.pendingEffectResolutionEntry != null) {
                gameData.pendingEffectResolutionEntry.setEventValue(2);
            }
        } else {
            if (gameData.pendingEffectResolutionEntry != null) {
                gameData.pendingEffectResolutionEntry.setEventValue(0);
            }
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " chooses not to put two opponent-owned exiled cards into their owners' graveyards."));
        }

        inputCompletionService.processMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    /**
     * A face-down pick names only its pile (what exiled the cards and when): the card that moves is chosen
     * at random from that pile, two picks of one pile taking two different cards (2015-08-25 ruling).
     */
    private static List<ExiledCardEntry> withFaceDownPicksAtRandom(GameData gameData, List<ExiledCardEntry> picks) {
        List<ExiledCardEntry> result = new ArrayList<>();
        for (ExiledCardEntry pick : picks) {
            if (!pick.faceDown()) {
                result.add(pick);
                continue;
            }
            List<ExiledCardEntry> pile = gameData.exiledCards.stream()
                    .filter(exiled -> PutOpponentOwnedExiledCardIntoGraveyardAndGainLifeEffectHandler
                            .sameFaceDownPile(pick, exiled))
                    .filter(exiled -> result.stream().noneMatch(taken -> taken.card().getId().equals(exiled.card().getId())))
                    .toList();
            result.add(pile.isEmpty() ? pick
                    : pile.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(pile.size())));
        }
        return result;
    }
}
