package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveDelayedPermanentActionEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Applies a scheduled action only when its delayed ability resolves.
 *
 * <p>A delayed sacrifice or destroy is a departure. The trigger's run of consecutive sacrifice (or destroy) steps is
 * one event (one delayed trigger "sacrifices those tokens"), so the first step of the run departs all of their
 * permanents as one batch through {@link StepDepartureSupport#departThenContinue}: a CR 616.1 question ends that step
 * before any of them leaves, and the later steps of the run find their permanent among its results and do nothing.
 * Once they have left, a sacrificed permanent's exiled card is returned. Other moves go through
 * {@link PermanentRemovalService#resolveDelayedPermanentAction}.
 */
@Component
@RequiredArgsConstructor
public class ResolveDelayedPermanentActionEffectHandler implements NormalEffectHandlerBean, ContinuesAfterDeparture {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ResolveDelayedPermanentActionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DelayedPermanentAction action = ((ResolveDelayedPermanentActionEffect) effect).action();
        DelayedPermanentActionKind.Op op = action.kind().op();
        if (op != DelayedPermanentActionKind.Op.SACRIFICE && op != DelayedPermanentActionKind.Op.DESTROY) {
            permanentRemovalService.resolveDelayedPermanentAction(gameData, action);
            return;
        }
        if (entry.getResolutionResults().departures().stream()
                .anyMatch(result -> result.permanentId().equals(action.permanentId()))) {
            return;
        }
        List<DepartureIntent> intents = new ArrayList<>();
        for (DelayedPermanentAction member : run(entry, effect, op)) {
            DepartureIntent intent = intent(gameData, member);
            if (intent != null && intents.stream().noneMatch(added -> added.permanentId().equals(intent.permanentId()))) {
                intents.add(intent);
            }
        }
        if (intents.isEmpty()) {
            return;
        }
        stepDepartureSupport.departThenContinue(gameData, entry, effect, DepartureBatch.of(intents), this);
    }

    /** Returns the exiled card of each delayed sacrifice of the run whose permanent was sacrificed. */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        for (DepartureResult result : entry.getResolutionResults().departures()) {
            if (!result.sacrificed()) {
                continue;
            }
            for (CardEffect step : entry.getEffectsToResolve()) {
                if (step instanceof ResolveDelayedPermanentActionEffect resolve
                        && resolve.action().permanentId().equals(result.permanentId())
                        && resolve.action().kind().op() == DelayedPermanentActionKind.Op.SACRIFICE) {
                    permanentRemovalService.returnExiledCardToBattlefield(gameData,
                            resolve.action().returnExiledCardId(), resolve.action().sacrificingPlayerId());
                    break;
                }
            }
        }
    }

    /**
     * The actions of {@code effect} and the steps right after it that do the same operation, when {@code effect} is
     * the step being resolved; otherwise {@code effect}'s action alone.
     */
    private static List<DelayedPermanentAction> run(StackEntry entry, CardEffect effect,
                                                    DelayedPermanentActionKind.Op op) {
        List<CardEffect> steps = entry.getEffectsToResolve();
        int index = entry.getResolvingEffectIndex();
        List<DelayedPermanentAction> run = new ArrayList<>();
        run.add(((ResolveDelayedPermanentActionEffect) effect).action());
        if (index < 0 || index >= steps.size() || steps.get(index) != effect) {
            return run;
        }
        for (int i = index + 1; i < steps.size(); i++) {
            if (!(steps.get(i) instanceof ResolveDelayedPermanentActionEffect next)
                    || next.action().kind().op() != op) {
                break;
            }
            run.add(next.action());
        }
        return run;
    }

    /** The departure of {@code action}'s permanent, or {@code null} when the action does nothing to it. */
    private DepartureIntent intent(GameData gameData, DelayedPermanentAction action) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, action.permanentId());
        if (permanent == null) {
            return null;
        }
        if (action.kind().op() == DelayedPermanentActionKind.Op.DESTROY) {
            return DepartureIntent.destroy(permanent.getId(), action.cannotBeRegenerated());
        }
        if (!gameQueryService.delayedTriggeredAbilityCanMoveCreatureToken(gameData, permanent)) {
            return null;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        if (action.sacrificingPlayerId() != null && !action.sacrificingPlayerId().equals(controllerId)) {
            return null;
        }
        if (gameQueryService.cantBeSacrificed(gameData, permanent)) {
            return null;
        }
        return DepartureIntent.sacrifice(permanent.getId());
    }
}
