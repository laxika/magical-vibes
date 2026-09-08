package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles a player's choice for Rift Elemental's mixed time-counter cost. */
@Component
@RequiredArgsConstructor
public class RemoveTimeCounterCostChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.RemoveTimeCounterCostChoice> {

    private final AbilityActivationService abilityActivationService;

    @Override
    public Class<PendingInteraction.RemoveTimeCounterCostChoice> handledType() {
        return PendingInteraction.RemoveTimeCounterCostChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.RemoveTimeCounterCostChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice");
        }
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (cardIds == null || cardIds.size() != 1
                || !interaction.validCardIds().contains(cardIds.getFirst())) {
            throw new IllegalStateException("Choose one valid permanent or suspended card");
        }
        abilityActivationService.completeActivatedAbilityTimeCounterCostChoice(
                gameData, player, interaction.context(), cardIds.getFirst());
    }
}
