package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutOpponentOwnedExiledCardIntoGraveyardCostChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice> {

    private final AbilityActivationService abilityActivationService;

    @Override
    public Class<PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice> handledType() {
        return PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your choice");
        }

        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosen == null || chosen.size() != 1 || !interaction.validCardIds().contains(chosen.getFirst())) {
            throw new IllegalStateException("Choose exactly one card an opponent owns from exile");
        }

        abilityActivationService.handlePutOpponentOwnedExiledCardIntoGraveyardCostChosen(
                gameData, player, interaction, chosen.getFirst());
    }
}
