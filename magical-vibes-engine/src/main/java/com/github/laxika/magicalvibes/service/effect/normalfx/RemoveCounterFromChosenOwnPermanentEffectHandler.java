package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromChosenOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RemoveCounterFromChosenOwnPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromChosenOwnPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.setEventValue(0);
        RemoveCounterFromChosenOwnPermanentEffect removeEffect =
                (RemoveCounterFromChosenOwnPermanentEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        FilterContext filterContext = filterContext(gameData, entry, controllerId);
        List<UUID> eligibleIds = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(
                    permanent, removeEffect.permanentFilter(), filterContext)) {
                eligibleIds.add(permanent.getId());
            }
        }
        if (eligibleIds.isEmpty()) {
            return;
        }

        PermanentChoiceContext.RemoveCounterFromChosenOwnPermanent context =
                new PermanentChoiceContext.RemoveCounterFromChosenOwnPermanent(removeEffect.permanentFilter());
        if (eligibleIds.size() == 1) {
            completeChoice(gameData, entry, eligibleIds.getFirst(), context, false);
            return;
        }

        playerInputService.beginPermanentChoice(gameData, controllerId, eligibleIds, context,
                "Choose a creature or planeswalker to remove a counter from.");
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.RemoveCounterFromChosenOwnPermanent context) {
        completeChoice(gameData, gameData.pendingEffectResolutionEntry, permanentId, context, true);
    }

    private void completeChoice(GameData gameData, StackEntry entry, UUID permanentId,
                                PermanentChoiceContext.RemoveCounterFromChosenOwnPermanent context,
                                boolean resumeAfterChoice) {
        if (entry == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (target == null
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !predicateEvaluationService.matchesPermanentPredicate(
                        target, context.permanentFilter(), filterContext(gameData, entry, entry.getControllerId()))) {
            resumeIfNeeded(gameData, resumeAfterChoice);
            return;
        }

        entry.setChosenPermanentId(target.getId());
        List<CounterType> counterTypes = RemoveChosenCountersFromTargetPermanentEffectHandler.counterTypesOn(target);
        if (counterTypes.isEmpty()) {
            resumeIfNeeded(gameData, resumeAfterChoice);
            return;
        }
        if (counterTypes.size() == 1) {
            removeCounter(gameData, entry, target, counterTypes.getFirst());
            resumeIfNeeded(gameData, resumeAfterChoice);
            return;
        }

        playerInputService.beginRemoveOneCounterChoice(gameData, entry.getControllerId(), target.getId(),
                entry.getCard().getName(), counterTypes);
    }

    private void resumeIfNeeded(GameData gameData, boolean resumeAfterChoice) {
        if (resumeAfterChoice) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    private void removeCounter(GameData gameData, StackEntry entry, Permanent target, CounterType counterType) {
        if (target.getCounterCount(counterType) > 0) {
            permanentCounterSupport.removeCounterFromPermanent(gameData, target, counterType, 1);
            entry.setEventValue(1);
        }
    }

    private static FilterContext filterContext(GameData gameData, StackEntry entry, UUID controllerId) {
        return FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(controllerId)
                .withXValue(entry.getXValue());
    }
}
