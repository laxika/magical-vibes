package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Coordinates the repeated per-object choices required by time travel. */
@Service
@RequiredArgsConstructor
public class TimeTravelService {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final RemoveTimeCounterFromExiledCardEffectHandler removeTimeCounterHandler;
    private final RemoveSuspendCounterFromExiledSpellEffectHandler removeSuspendCounterHandler;
    private final PlayerInputService playerInputService;
    private final InputCompletionService inputCompletionService;

    public void begin(GameData gameData, StackEntry entry, int times) {
        List<ChoiceContext.TimeTravelTarget> targets = eligibleTargets(gameData, entry.getControllerId());
        if (!targets.isEmpty()) {
            beginChoice(gameData, entry.getControllerId(), entry.getCard().getName(), targets, 0, times);
        }
    }

    public void handleChoice(GameData gameData, String choice,
                             ChoiceContext.TimeTravelActionChoice context) {
        if (!ChoiceContext.TimeTravelActionChoice.OPTIONS.contains(choice)) {
            throw new IllegalArgumentException("Invalid time-travel action: " + choice);
        }

        gameData.interaction.clearAwaitingInput();
        if (!ChoiceContext.TimeTravelActionChoice.SKIP.equals(choice)) {
            applyChoice(gameData, choice, context);
        }

        int nextTargetIndex = context.targetIndex() + 1;
        if (nextTargetIndex < context.targets().size()) {
            beginChoice(gameData, context.controllerId(), context.sourceCardName(), context.targets(),
                    nextTargetIndex, context.remainingTravels());
            return;
        }

        if (context.remainingTravels() > 1) {
            List<ChoiceContext.TimeTravelTarget> nextTargets = eligibleTargets(gameData, context.controllerId());
            if (!nextTargets.isEmpty()) {
                beginChoice(gameData, context.controllerId(), context.sourceCardName(), nextTargets,
                        0, context.remainingTravels() - 1);
                return;
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
    }

    private void beginChoice(GameData gameData, UUID controllerId, String sourceCardName,
                             List<ChoiceContext.TimeTravelTarget> targets, int targetIndex,
                             int remainingTravels) {
        playerInputService.beginTimeTravelChoice(gameData,
                new ChoiceContext.TimeTravelActionChoice(
                        controllerId, sourceCardName, targets, targetIndex, remainingTravels));
    }

    private List<ChoiceContext.TimeTravelTarget> eligibleTargets(GameData gameData, UUID controllerId) {
        List<ChoiceContext.TimeTravelTarget> targets = new ArrayList<>();
        gameData.forEachPermanent((permanentControllerId, permanent) -> {
            if (Objects.equals(controllerId, permanentControllerId)
                    && permanent.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.TIME) > 0) {
                targets.add(new ChoiceContext.TimeTravelTarget(permanent.getId(), Zone.BATTLEFIELD));
            }
        });

        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                UUID cardId = exiled.card().getId();
                Integer counters = gameData.exiledCardTimeCounters.get(cardId);
                if (Objects.equals(controllerId, exiled.ownerId())
                        && !exiled.faceDown() && counters != null && counters > 0
                        && !gameData.exiledCardsWithNonSuspendTimeCounters.contains(cardId)) {
                    targets.add(new ChoiceContext.TimeTravelTarget(cardId, Zone.EXILE));
                }
            }
        }
        for (GameData.SuspendedSpellExile suspended : gameData.suspendedSpellExiles) {
            if (Objects.equals(controllerId, suspended.ownerId()) && suspended.counters() > 0
                    && gameData.findExiledCard(suspended.cardId()) != null
                    && targets.stream().noneMatch(target -> target.id().equals(suspended.cardId()))) {
                targets.add(new ChoiceContext.TimeTravelTarget(suspended.cardId(), Zone.EXILE));
            }
        }
        return targets;
    }

    private void applyChoice(GameData gameData, String choice,
                             ChoiceContext.TimeTravelActionChoice context) {
        ChoiceContext.TimeTravelTarget selected = context.target();
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (selected.zone() == Zone.EXILE) {
            ExiledCardEntry exiled = gameData.findExiledCard(selected.id());
            for (int i = 0; i < gameData.suspendedSpellExiles.size(); i++) {
                GameData.SuspendedSpellExile suspended = gameData.suspendedSpellExiles.get(i);
                if (!suspended.cardId().equals(selected.id())) continue;
                if (exiled == null || !Objects.equals(suspended.ownerId(), context.controllerId())) return;
                if (ChoiceContext.TimeTravelActionChoice.ADD.equals(choice)) {
                    gameData.suspendedSpellExiles.set(i, new GameData.SuspendedSpellExile(
                            suspended.cardId(), suspended.ownerId(), suspended.counters() + 1));
                } else {
                    removeSuspendCounterHandler.resolve(gameData, entry,
                            new com.github.laxika.magicalvibes.model.effect.RemoveSuspendCounterFromExiledSpellEffect(selected.id()));
                }
                return;
            }
            Integer counters = gameData.exiledCardTimeCounters.get(selected.id());
            if (exiled == null || exiled.faceDown() || !Objects.equals(exiled.ownerId(), context.controllerId())
                    || counters == null || counters <= 0) {
                return;
            }
            if (ChoiceContext.TimeTravelActionChoice.ADD.equals(choice)) {
                gameData.exiledCardTimeCounters.merge(selected.id(), 1, Integer::sum);
            } else {
                removeTimeCounterHandler.removeTimeCounter(gameData, selected.id());
            }
            return;
        }

        Permanent permanent = gameQueryService.findPermanentById(gameData, selected.id());
        if (permanent == null
                || !Objects.equals(context.controllerId(),
                gameQueryService.findPermanentController(gameData, permanent.getId()))) {
            return;
        }
        if (ChoiceContext.TimeTravelActionChoice.ADD.equals(choice)) {
            if (permanent.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.TIME) > 0) {
                permanentCounterSupport.placeCounterOnPermanent(gameData, entry, permanent,
                        com.github.laxika.magicalvibes.model.CounterType.TIME, 1);
            }
        } else if (permanent.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.TIME) > 0) {
            permanentCounterSupport.removeCountersFromPermanent(gameData, permanent,
                    com.github.laxika.magicalvibes.model.CounterType.TIME, 1);
        }
    }
}
