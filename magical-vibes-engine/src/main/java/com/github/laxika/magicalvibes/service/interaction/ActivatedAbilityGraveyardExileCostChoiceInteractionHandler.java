package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles selecting any number of cards to pay an activated ability's graveyard exile cost. */
@Component
@RequiredArgsConstructor
public class ActivatedAbilityGraveyardExileCostChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.ActivatedAbilityGraveyardExileCostChoice> {

    private final AbilityActivationService abilityActivationService;

    @Override
    public Class<PendingInteraction.ActivatedAbilityGraveyardExileCostChoice> handledType() {
        return PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.ActivatedAbilityGraveyardExileCostChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        abilityActivationService.handleActivatedAbilityGraveyardExileCostChosen(gameData, player, interaction, cardIds);
    }
}
