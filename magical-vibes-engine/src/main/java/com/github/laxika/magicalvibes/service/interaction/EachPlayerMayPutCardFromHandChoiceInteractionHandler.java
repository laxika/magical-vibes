package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachPlayerMayPutCardFromHandToBattlefieldSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerMayPutCardFromHandChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.EachPlayerMayPutCardFromHandChoice> {

    private final EachPlayerMayPutCardFromHandToBattlefieldSupport support;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.EachPlayerMayPutCardFromHandChoice> handledType() {
        return PendingInteraction.EachPlayerMayPutCardFromHandChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.EachPlayerMayPutCardFromHandChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosen == null) {
            chosen = List.of();
        }
        int maxCount = interaction.anyNumber() && !interaction.repeatUntilNoOne()
                ? interaction.validCardIds().size() : 1;
        if (chosen.size() > maxCount || !interaction.validCardIds().containsAll(chosen)
                || chosen.stream().distinct().count() != chosen.size()) {
            throw new IllegalStateException("Choose zero to " + maxCount + " valid cards");
        }

        boolean cardPutThisRound = interaction.cardPutThisRound();
        List<UUID> accumulated = new ArrayList<>(interaction.chosenCardIds());
        if (interaction.repeatUntilNoOne()) {
            if (!chosen.isEmpty()) {
                cardPutThisRound = support.putCardOntoBattlefield(
                        gameData, player.getId(), chosen.getFirst(), interaction.cardName())
                        || cardPutThisRound;
            }
        } else {
            accumulated.addAll(chosen);
        }
        gameData.interaction.clearAwaitingInput();

        EachPlayerMayPutCardFromHandToBattlefieldEffect effect =
                new EachPlayerMayPutCardFromHandToBattlefieldEffect(interaction.predicate(), interaction.label(),
                        false, interaction.repeatUntilNoOne(), interaction.startingPlayerId() != null,
                        interaction.anyNumber());
        boolean begunNext = support.beginNextChoice(gameData, interaction.remainingPlayerIds(), accumulated,
                effect, interaction.cardName(), cardPutThisRound, interaction.startingPlayerId());
        inputCompletionService.publishStateAfterInput(gameData);
        if (!begunNext) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }
}
