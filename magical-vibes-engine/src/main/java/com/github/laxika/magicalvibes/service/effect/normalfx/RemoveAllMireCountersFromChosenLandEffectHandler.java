package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllMireCountersFromChosenLandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Cyclopean Tomb's non-targeting choice of one tracked land. */
@Component
public class RemoveAllMireCountersFromChosenLandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    public RemoveAllMireCountersFromChosenLandEffectHandler(
            GameQueryService gameQueryService,
            GameLogService gameLogService,
            InputCompletionService inputCompletionService,
            PermanentCounterSupport permanentCounterSupport,
            PlayerInputService playerInputService,
            PredicateEvaluationService predicateEvaluationService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.inputCompletionService = inputCompletionService;
        this.permanentCounterSupport = permanentCounterSupport;
        this.playerInputService = playerInputService;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllMireCountersFromChosenLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var cleanup = (RemoveAllMireCountersFromChosenLandEffect) effect;
        List<UUID> eligibleIds = eligibleIds(gameData, cleanup.permanentFilter());
        if (eligibleIds.isEmpty()) {
            return;
        }

        PermanentChoiceContext.CyclopeanTombUpkeepLandChoice context =
                new PermanentChoiceContext.CyclopeanTombUpkeepLandChoice(
                        cleanup.delayedActionId(), cleanup.permanentFilter());
        if (eligibleIds.size() == 1) {
            completeChoice(gameData, entry, eligibleIds.getFirst(), context, false);
            return;
        }

        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), eligibleIds, context,
                entry.getCard().getName() + "'s delayed ability — Choose a land.");
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.CyclopeanTombUpkeepLandChoice context) {
        completeChoice(gameData, gameData.pendingEffectResolutionEntry, permanentId, context, true);
    }

    private void completeChoice(GameData gameData, StackEntry entry, UUID permanentId,
                                PermanentChoiceContext.CyclopeanTombUpkeepLandChoice context,
                                boolean resumeAfterChoice) {
        Permanent target = gameQueryService.findPermanentById(gameData, permanentId);
        if (entry == null || target == null
                || !gameQueryService.isLand(gameData, target)
                || !predicateEvaluationService.matchesPermanentPredicate(
                        gameData, target, context.permanentFilter())) {
            if (resumeAfterChoice) {
                inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
            }
            return;
        }

        int mireCounters = target.getCounterCount(CounterType.MIRE);
        if (mireCounters > 0) {
            permanentCounterSupport.removeCounterFromPermanent(
                    gameData, target, CounterType.MIRE, mireCounters);
            gameLogService.append(gameData, GameLog.text(
                    "All mire counters are removed from " + target.getCard().getName() + "."));
        }
        gameData.markCyclopeanTombLandRemoved(context.delayedActionId(), target.getId());

        if (resumeAfterChoice) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    private List<UUID> eligibleIds(GameData gameData,
                                   com.github.laxika.magicalvibes.model.filter.PermanentPredicate filter) {
        List<UUID> eligibleIds = new ArrayList<>();
        gameData.forEachPermanent((ignored, permanent) -> {
            if (gameQueryService.isLand(gameData, permanent)
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter)) {
                eligibleIds.add(permanent.getId());
            }
        });
        return eligibleIds;
    }
}
