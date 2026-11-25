package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves {@link RemoveCountersEffect}: removes up to the fixed or dynamically evaluated amount of counters of the
 * given type from each permanent its subject names, clamped at zero, each through
 * {@link PermanentCounterSupport#removeCounterFromPermanent} (the last time counter triggers vanishing, CR 702.63a)
 * and reported as removed by the ability's controller ("when you remove the last ... counter"); a Siege that lost its
 * last defense counter queues its defeat (CR 310.12b). A dynamic amount is evaluated against the ability's source for
 * a target, else against each permanent. A counter of {@link CounterType#ANY} kind comes off the one kind the
 * permanent has enough of, or the kind its controller chooses among several: the chosen kind's removal is spliced in
 * as the next step and records the step.
 */
@Component
@RequiredArgsConstructor
public class RemoveCountersEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;
    private final BattleDefeatSupport battleDefeatSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCountersEffect) effect;
        List<Permanent> permanents = subjectResolutionService.permanents(gameData, entry, effect, e.subject());
        if (e.counterType() == CounterType.ANY) {
            removeCounterOfAnyKind(gameData, entry, e, permanents);
            return;
        }
        // A REMOVE_COUNTERS step: "remove a counter from it. If you do" reads how many came off (never prompts).
        Map<UUID, Integer> before = permanentCounterSupport.counterCounts(gameData,
                permanents.stream().map(Permanent::getId).toList(), e.counterType());
        Permanent amountSource = e.dynamicAmount() != null && e.subject() instanceof PermanentSubject.Target
                ? source(gameData, entry) : null;
        for (Permanent permanent : permanents) {
            int amount = e.dynamicAmount() != null
                    ? amountEvaluationService.evaluate(gameData, e.dynamicAmount(), AmountContext.forStackEntry(entry,
                    e.subject() instanceof PermanentSubject.Target ? amountSource : permanent))
                    : e.amount();
            int countBefore = permanent.getCounterCount(e.counterType());
            permanentCounterSupport.removeCounterFromPermanent(gameData, permanent, e.counterType(), amount);
            int removed = countBefore - permanent.getCounterCount(e.counterType());
            permanentCounterSupport.notifyLastCounterRemoved(gameData, permanent, e.counterType(), removed,
                    entry.getControllerId());
            if (e.counterType() == CounterType.DEFENSE && removed > 0) {
                battleDefeatSupport.checkAfterDefenseRemoved(gameData, permanent);
            }
        }
        entry.recordStep(StepKind.REMOVE_COUNTERS,
                permanentCounterSupport.counterChange(gameData, before, e.counterType(), false));
    }

    /** The ability's source, or its last-known information once it left. */
    private Permanent source(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        return source != null ? source : entry.getSourcePermanentSnapshot();
    }

    private void removeCounterOfAnyKind(GameData gameData, StackEntry entry, RemoveCountersEffect e,
                                        List<Permanent> permanents) {
        entry.recordStep(StepKind.REMOVE_COUNTERS, StepOutcome.NONE);
        if (permanents.isEmpty()) {
            return;
        }
        List<CounterType> kinds = PermanentCounterSupport.removableCounterKinds(permanents.getFirst(), e.amount());
        if (kinds.size() == 1) {
            resolve(gameData, entry, e.withKind(kinds.getFirst()));
        } else if (kinds.size() > 1) {
            var options = kinds.stream()
                    .map(kind -> new ChooseOneEffect.ChooseOneOption(
                            "Remove " + e.amount() + " " + permanentCounterSupport.counterTypeName(kind) + " counters",
                            e.withKind(kind)))
                    .toList();
            playerInputService.beginChooseModeChoice(gameData, entry.getControllerId(), entry.getCard(),
                    new ChooseOneEffect(options), entry.getSourcePermanentId());
        }
    }
}
