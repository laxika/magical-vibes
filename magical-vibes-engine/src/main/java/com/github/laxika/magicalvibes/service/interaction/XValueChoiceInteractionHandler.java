package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Handles X value choice interactions: prompts the deciding player, validates the chosen
 * value, stores it on GameData, and resumes effect resolution. The actual game logic (what
 * to do with the chosen X) lives in the effect handler that initiated the interaction
 * (e.g. PayXManaGainXLifeEffectHandler), which re-runs and reads {@code chosenXValue}. An
 * activated ability's variable source-counter cost instead resumes through
 * {@link AbilityActivationService}.
 */
@Slf4j
@Component
public class XValueChoiceInteractionHandler implements InteractionHandler<PendingInteraction.XValueChoice> {

    private final InputCompletionService inputCompletionService;
    private final AbilityActivationService abilityActivationService;

    @Autowired
    public XValueChoiceInteractionHandler(InputCompletionService inputCompletionService,
                                          AbilityActivationService abilityActivationService) {
        this.inputCompletionService = inputCompletionService;
        this.abilityActivationService = abilityActivationService;
    }

    @Override
    public Class<PendingInteraction.XValueChoice> handledType() {
        return PendingInteraction.XValueChoice.class;
    }

    @Override
    public Class<? extends InteractionAnswer> answerType() {
        return InteractionAnswer.NumberChosen.class;
    }

    @Override
    public void handleAnswer(GameData gameData, Player player, PendingInteraction.XValueChoice interaction,
                             InteractionAnswer answer) {
        int chosenValue = ((InteractionAnswer.NumberChosen) answer).value();
        if (!player.getId().equals(interaction.playerId())) {
            throw new IllegalStateException("Not your turn to choose");
        }

        int maxAllowed = interaction.maxValue();
        if (chosenValue < interaction.minValue() || chosenValue > maxAllowed) {
            throw new IllegalArgumentException("Value must be between " + interaction.minValue()
                    + " and " + maxAllowed);
        }

        if (gameData.pendingAbilityCounterCostActivation != null) {
            abilityActivationService.handleActivatedAbilityCounterCostChosen(gameData, player, chosenValue);
            return;
        }

        // Store chosen value for the effect handler to use on re-entry
        gameData.chosenXValue = chosenValue;
        gameData.interaction.clearAwaitingInput();

        // Resume the parked effect and publish only after SBA/auto-pass reach a stable point.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
