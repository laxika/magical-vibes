package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles selecting graveyard cards to put on the bottom of a library as an activation cost. */
@Component
@RequiredArgsConstructor
public class ActivatedAbilityGraveyardLibraryCostChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice> {

    private final AbilityActivationService abilityActivationService;

    @Override
    public Class<PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice> handledType() {
        return PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ActivatedAbilityGraveyardLibraryCostChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        abilityActivationService.handleActivatedAbilityGraveyardLibraryCostChosen(
                gameData, player, interaction, cardIds);
    }
}
