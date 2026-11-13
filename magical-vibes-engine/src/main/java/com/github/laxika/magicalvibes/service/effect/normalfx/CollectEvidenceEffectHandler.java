package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceEffect;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves the optional graveyard selection that collects evidence. */
@Component
@RequiredArgsConstructor
public class CollectEvidenceEffectHandler implements NormalEffectHandlerBean {

    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final PlayerInputService playerInputService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CollectEvidenceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CollectEvidenceEffect collectEvidence = (CollectEvidenceEffect) effect;
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        int minimumManaValue = collectEvidence.minimumManaValue();
        if (collectEvidence.chooseX()) {
            int available = graveyard == null ? 0 : graveyard.stream().mapToInt(Card::getManaValue).sum();
            if (gameData.chosenXValue == null && available > 0) {
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                        entry.getControllerId(), available,
                        entry.getCard().getName() + " — Choose X for collect evidence X.",
                        entry.getCard().getName()));
                return;
            }
            minimumManaValue = gameData.chosenXValue == null ? 0 : gameData.chosenXValue;
            gameData.chosenXValue = null;
            entry.setEventValue(minimumManaValue);
            if (minimumManaValue == 0) {
                triggerCollectionService.checkCollectEvidenceTriggers(gameData, entry.getControllerId());
                queueReflexiveAbility(gameData, entry, collectEvidence.thenEffect());
                return;
            }
        }
        if (minimumManaValue > 0
                && (graveyard == null || graveyard.isEmpty()
                || graveyard.stream().mapToInt(Card::getManaValue).sum() < minimumManaValue)) {
            return;
        }

        if (minimumManaValue == 0 && (graveyard == null || graveyard.isEmpty())) {
            entry.setEventValue(0);
            triggerCollectionService.checkCollectEvidenceTriggers(gameData, entry.getControllerId());
            queueReflexiveAbility(gameData, entry, collectEvidence.thenEffect());
            return;
        }

        gameData.graveyardTargetOperation.resolutionTimeCollectEvidenceResume = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInputService.beginMultiGraveyardChoiceWithMinimumManaValue(
                gameData, entry.getControllerId(), new ArrayList<>(graveyard), graveyard.size(),
                minimumManaValue,
                entry.getCard().getName() + " — You may collect evidence "
                        + minimumManaValue + ". Choose cards from your graveyard.");
    }

    public void queueReflexiveAbility(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        if (thenEffect == null) {
            return;
        }
        // "When you do" is an ability of its own (CR 603.12): the stack door chooses its target as it goes on the stack.
        triggeredAbilityStackService.putMade(gameData, ReflexiveAbilitySupport.record(gameData, entry, List.of(thenEffect)));
    }

}
