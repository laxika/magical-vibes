package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffect;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EspersToMagiciteCreatureChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.EspersToMagiciteCreatureChoice> {

    private final InputCompletionService inputCompletionService;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;

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
            // "When you do, choose up to one target creature card exiled this way": the reflexive ability (CR 603.12)
            // goes on the stack through the stack door with the card chosen as it went there (CR 603.3d).
            StackEntry reflexiveAbility = TriggeredAbilityStackService.madeAbility(
                    interaction.sourceCard(),
                    player.getId(),
                    interaction.sourceCard().getName() + "'s reflexive ability",
                    List.of(new ExileOpponentsGraveyardsAndCreateArtifactTokenCopyEffect()),
                    interaction.validCardIds());
            reflexiveAbility.setTargetId(chosenIds.getFirst());
            reflexiveAbility.setNonTargeting(true);
            triggeredAbilityStackService.putMade(gameData, reflexiveAbility);
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
