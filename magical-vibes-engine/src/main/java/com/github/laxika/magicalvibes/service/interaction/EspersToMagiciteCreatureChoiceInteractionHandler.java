package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffect;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EspersToMagiciteCreatureChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.EspersToMagiciteCreatureChoice> {

    private final InputCompletionService inputCompletionService;

    @Override
    public Class<PendingInteraction.EspersToMagiciteCreatureChoice> handledType() {
        return PendingInteraction.EspersToMagiciteCreatureChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.EspersToMagiciteCreatureChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> chosenIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosenIds == null || chosenIds.size() > 1
                || chosenIds.stream().anyMatch(cardId -> !interaction.validCardIds().contains(cardId))) {
            throw new IllegalStateException("Choose up to one creature card exiled this way");
        }

        gameData.interaction.clearAwaitingInput();
        if (!chosenIds.isEmpty()) {
            StackEntry reflexiveAbility = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    interaction.sourceCard(),
                    player.getId(),
                    interaction.sourceCard().getName() + "'s reflexive ability",
                    List.of(new ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffect()),
                    (UUID) null,
                    List.of());
            reflexiveAbility.setTargetCardIds(interaction.validCardIds());
            reflexiveAbility.setTargetId(chosenIds.getFirst());
            reflexiveAbility.setNonTargeting(true);
            gameData.stack.add(reflexiveAbility);
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
