package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AsEntersDiscardEntry;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.CopyDrawnInstantOrSorceryAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCardFromOutsideGameWithNormalCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithCostReductionEffect;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.effect.mayfx.CopyDrawnInstantOrSorceryAndMayCastCopyHandler;
import com.github.laxika.magicalvibes.service.effect.mayfx.ExileTargetCardFromGraveyardAndMayCastCopyHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileNormalCostCopySupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileReducedCastSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.OutsideGameNormalCostCastSupport;
import com.github.laxika.magicalvibes.service.battlefield.AsEntersDiscardSupport;
import com.github.laxika.magicalvibes.service.input.ChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import org.springframework.beans.factory.ObjectProvider;
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
    private final ObjectProvider<MayCastHandlerService> mayCastHandlerService;
    private final ObjectProvider<ExileTargetCardFromGraveyardAndMayCastCopyHandler> graveyardCopyHandler;
    private final ObjectProvider<ExileNormalCostCopySupport> exileNormalCostCopySupport;
    private final ObjectProvider<AsEntersDiscardSupport> asEntersDiscardSupport;
    private final ObjectProvider<ExileReducedCastSupport> exileReducedCastSupport;
    private final ObjectProvider<CopyDrawnInstantOrSorceryAndMayCastCopyHandler> copyDrawnCastHandler;
    private final ObjectProvider<OutsideGameNormalCostCastSupport> outsideGameNormalCostCastSupport;
    private final ObjectProvider<ChoiceHandlerService> choiceHandlerService;

    @Autowired
    public XValueChoiceInteractionHandler(InputCompletionService inputCompletionService,
                                          AbilityActivationService abilityActivationService,
                                          ObjectProvider<MayCastHandlerService> mayCastHandlerService,
                                          ObjectProvider<ExileTargetCardFromGraveyardAndMayCastCopyHandler> graveyardCopyHandler,
                                          ObjectProvider<ExileNormalCostCopySupport> exileNormalCostCopySupport,
                                          ObjectProvider<AsEntersDiscardSupport> asEntersDiscardSupport,
                                          ObjectProvider<ExileReducedCastSupport> exileReducedCastSupport,
                                          ObjectProvider<CopyDrawnInstantOrSorceryAndMayCastCopyHandler> copyDrawnCastHandler,
                                          ObjectProvider<OutsideGameNormalCostCastSupport> outsideGameNormalCostCastSupport,
                                          ObjectProvider<ChoiceHandlerService> choiceHandlerService) {
        this.choiceHandlerService = choiceHandlerService;
        this.outsideGameNormalCostCastSupport = outsideGameNormalCostCastSupport;
        this.exileReducedCastSupport = exileReducedCastSupport;
        this.copyDrawnCastHandler = copyDrawnCastHandler;
        this.asEntersDiscardSupport = asEntersDiscardSupport;
        this.graveyardCopyHandler = graveyardCopyHandler;
        this.exileNormalCostCopySupport = exileNormalCostCopySupport;
        this.inputCompletionService = inputCompletionService;
        this.abilityActivationService = abilityActivationService;
        this.mayCastHandlerService = mayCastHandlerService;
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

        if (interaction.distribution() != null) {
            choiceHandlerService.getObject().applyCounterDistributionAssignment(
                    gameData, player, chosenValue, interaction.distribution());
            return;
        }
        if (gameData.pendingAbilityCounterCostActivation != null) {
            abilityActivationService.handleActivatedAbilityCounterCostChosen(gameData, player, chosenValue);
            return;
        }
        if (interaction.graveyardCastAbility() != null) {
            var ability = interaction.graveyardCastAbility();
            if (ability.effects().stream().anyMatch(MayCastExiledCardWithCostReductionEffect.class::isInstance)) {
                int reduction = ability.effects().stream()
                        .filter(MayCastExiledCardWithCostReductionEffect.class::isInstance)
                        .map(MayCastExiledCardWithCostReductionEffect.class::cast)
                        .findFirst().orElseThrow().genericCostReduction();
                gameData.interaction.clearAwaitingInput();
                exileReducedCastSupport.getObject().resumeCastWithX(
                        gameData, player, ability.targetCardId(), reduction, chosenValue);
                return;
            }
            if (ability.effects().stream().anyMatch(
                    CopyDrawnInstantOrSorceryAndMayCastCopyEffect.class::isInstance)) {
                gameData.interaction.clearAwaitingInput();
                copyDrawnCastHandler.getObject().resumeCastWithX(gameData, player, ability, chosenValue);
                return;
            }
            if (ability.effects().stream().anyMatch(
                    MayCastCardFromOutsideGameWithNormalCostEffect.class::isInstance)) {
                gameData.interaction.clearAwaitingInput();
                outsideGameNormalCostCastSupport.getObject().resumeCastWithX(
                        gameData, player, ability.targetCardId(), chosenValue);
                return;
            }
            if (ability.effects().stream().anyMatch(AsEntersDiscardEntry.class::isInstance)) {
                asEntersDiscardSupport.getObject().handleCount(gameData, player, ability, chosenValue);
                return;
            }
            if (ability.effects().stream().noneMatch(CastCardFromGraveyardEffect.class::isInstance)) {
                gameData.interaction.clearAwaitingInput();
                if (ability.effects().stream().anyMatch(
                        ExileTargetCardFromGraveyardAndMayCastCopyEffect.class::isInstance)) {
                    graveyardCopyHandler.getObject().resumeCastWithX(gameData, player, ability, chosenValue);
                } else {
                    exileNormalCostCopySupport.getObject().resumeCastWithX(gameData, player, ability, chosenValue);
                }
                return;
            }
            CastCardFromGraveyardEffect castEffect = ability.effects().stream()
                    .filter(CastCardFromGraveyardEffect.class::isInstance)
                    .map(CastCardFromGraveyardEffect.class::cast).findFirst().orElseThrow();
            gameData.interaction.clearAwaitingInput();
            mayCastHandlerService.getObject().handleCastCardFromGraveyardChoice(
                    gameData, player, true, ability, castEffect, chosenValue);
            return;
        }

        // Store chosen value for the effect handler to use on re-entry
        gameData.chosenXValue = chosenValue;
        gameData.interaction.clearAwaitingInput();

        // Resume the parked effect and publish only after SBA/auto-pass reach a stable point.
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
