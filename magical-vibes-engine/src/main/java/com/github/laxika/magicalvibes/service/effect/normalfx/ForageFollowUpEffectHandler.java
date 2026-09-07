package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ForageFollowUpEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a targeted forage rider after the forage action has succeeded. */
@Component
@RequiredArgsConstructor
public class ForageFollowUpEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ForageFollowUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ForageFollowUpEffect followUp = (ForageFollowUpEffect) effect;
        GraveyardTargetingSupport.Target target = graveyardTargetingSupport.findTarget(
                List.of(followUp.thenEffect()));
        if (target == null || target.scope() != GraveyardSearchScope.CONTROLLERS_GRAVEYARD) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(controllerId, List.of());
        List<Integer> matchingIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(
                    graveyard.get(i), target.filter(), entry.getCard().getId())) {
                matchingIndices.add(i);
            }
        }

        if (matchingIndices.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s forage ability has no valid graveyard targets."));
            return;
        }

        if (matchingIndices.size() == 1) {
            pushTargetedFollowUp(gameData, entry, followUp.thenEffect(),
                    graveyard.get(matchingIndices.getFirst()));
            return;
        }

        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, matchingIndices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        entry.getCard().getName() + "'s forage ability — Choose a permanent card from your graveyard to target.")
                .mayAbilityContext(entry.getCard(), controllerId,
                        List.of(followUp.thenEffect()), entry.getSourcePermanentId())
                .build());
    }

    private void pushTargetedFollowUp(GameData gameData, StackEntry sourceEntry,
                                      CardEffect thenEffect, Card targetCard) {
        StackEntry followUpEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceEntry.getCard(),
                sourceEntry.getControllerId(),
                sourceEntry.getCard().getName() + "'s ability",
                List.of(thenEffect),
                targetCard.getId(),
                Zone.GRAVEYARD,
                sourceEntry.getSourcePermanentId());
        followUpEntry.setSourcePermanentSnapshot(sourceEntry.getSourcePermanentSnapshot());
        followUpEntry.setEventValue(sourceEntry.getEventValue());
        gameData.stack.add(followUpEntry);
    }
}
