package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.effect.normalfx.IntuitionEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Answers {@link com.github.laxika.magicalvibes.model.effect.IntuitionEffect}'s search: the searcher (or Opposition
 * Agent's controller) picks the cards, exactly the required number, or for a search for "exactly" that many cards with
 * different names either that many or none (CR 701.23b). {@link IntuitionEffectHandler#found} then reveals and records
 * them, and the parked resolution goes on.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IntuitionSearchChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.IntuitionSearchChoice> {

    private final InputCompletionService inputCompletionService;

    @Autowired
    @Lazy
    private IntuitionEffectHandler intuitionEffectHandler;

    @Override
    public Class<PendingInteraction.IntuitionSearchChoice> handledType() {
        return PendingInteraction.IntuitionSearchChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.IntuitionSearchChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.decidingPlayerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null) {
            cardIds = List.of();
        }
        if (interaction.requireDifferentNames()
                ? !cardIds.isEmpty() && cardIds.size() != interaction.count()
                : cardIds.size() != interaction.count()) {
            throw new IllegalStateException("Must choose exactly " + interaction.count() + " cards"
                    + (interaction.requireDifferentNames() ? " or none" : ""));
        }

        List<UUID> validIds = interaction.validCardIds();
        Set<UUID> chosenIds = new HashSet<>();
        Set<String> chosenNames = new HashSet<>();
        for (UUID id : cardIds) {
            if (!validIds.contains(id)) {
                throw new IllegalStateException("Invalid card ID: " + id);
            }
            if (!chosenIds.add(id)) {
                throw new IllegalStateException("Duplicate card ID: " + id);
            }
            if (interaction.requireDifferentNames()) {
                Card card = interaction.pool().stream()
                        .filter(candidate -> candidate.getId().equals(id))
                        .findFirst()
                        .orElseThrow();
                if (!chosenNames.add(card.getName())) {
                    throw new IllegalStateException("Must choose cards with different names");
                }
            }
        }

        List<Card> chosen = new ArrayList<>();
        for (Card card : interaction.pool()) {
            if (chosenIds.contains(card.getId())) {
                chosen.add(card);
            }
        }

        gameData.interaction.clearAwaitingInput();
        intuitionEffectHandler.found(gameData, gameData.pendingEffectResolutionEntry, interaction.playerId(), chosen,
                interaction.decisionPlayerId());
        log.info("Game {} - library search: {} found {} cards",
                gameData.id, gameData.playerIdToName.get(interaction.playerId()), chosen.size());
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
