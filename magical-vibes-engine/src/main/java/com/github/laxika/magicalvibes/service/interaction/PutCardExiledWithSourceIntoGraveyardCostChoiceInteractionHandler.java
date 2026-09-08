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
public class PutCardExiledWithSourceIntoGraveyardCostChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice> {

    private final AbilityActivationService abilityActivationService;

    @Override
    public Class<PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice> handledType() {
        return PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.PutCardExiledWithSourceIntoGraveyardCostChoice interaction,
                             InteractionAnswer answer) {
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        List<UUID> chosen = ((InteractionAnswer.CardsChosen) answer).cardIds();
        if (chosen == null || chosen.size() != 1 || !interaction.validCardIds().contains(chosen.getFirst())) {
            throw new IllegalStateException("Choose exactly one card exiled with this permanent");
        }

        abilityActivationService.handlePutCardExiledWithSourceIntoGraveyardCostChosen(
                gameData, player, interaction, chosen.getFirst());
    }
}
