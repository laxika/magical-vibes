package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.ChooseCardFromGraveyardMessage;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handles the "exile a card from your graveyard as an activation cost" choice. The pending
 * activation itself lives in {@code GameData.pendingAbilityActivation}; the answer (cost
 * payment and ability activation) is applied by
 * {@link AbilityActivationService#handleActivatedAbilityGraveyardExileCostChosen}. Matching
 * the legacy begin site, no log line is emitted on prompt.
 */
@Component
@RequiredArgsConstructor
public class GraveyardExileCostChoiceInteractionHandler
        implements InteractionHandler<PendingInteraction.GraveyardExileCostChoice> {

    private final SessionManager sessionManager;
    private final AbilityActivationService abilityActivationService;

    @Override
    public Class<PendingInteraction.GraveyardExileCostChoice> handledType() {
        return PendingInteraction.GraveyardExileCostChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.GraveyardCardChosen.class;
    }

    @Override
    public void prompt(GameData gameData, PendingInteraction.GraveyardExileCostChoice interaction, UUID recipientId) {
        sessionManager.sendToPlayer(recipientId, new ChooseCardFromGraveyardMessage(
                interaction.validIndices(), interaction.prompt(), false));
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.GraveyardExileCostChoice interaction,
                             InteractionAnswer answer) {
        int cardIndex = ((InteractionAnswer.GraveyardCardChosen) answer).cardIndex();
        abilityActivationService.handleActivatedAbilityGraveyardExileCostChosen(gameData, player, cardIndex);
    }
}
