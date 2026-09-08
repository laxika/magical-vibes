package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Handles selecting the artifact used to pay a craft activation's material cost. */
@Component
@RequiredArgsConstructor
public class CraftMaterialChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.CraftMaterialChoice> {

    private final AbilityActivationService abilityActivationService;

    @Override
    public Class<PendingInteraction.CraftMaterialChoice> handledType() {
        return PendingInteraction.CraftMaterialChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.CardsChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player,
                             PendingInteraction.CraftMaterialChoice interaction,
                             InteractionAnswer answer) {
        List<UUID> cardIds = ((InteractionAnswer.CardsChosen) answer).cardIds();
        abilityActivationService.handleCraftMaterialChosen(gameData, player, interaction, cardIds);
    }
}
