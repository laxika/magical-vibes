package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
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
 * Resolves {@link RemoveCounterFromSourceEffect}: removes up to the fixed or dynamically evaluated
 * amount of counters of the given type from each permanent its subject names, clamped at zero, and
 * reports the removal by the ability's controller ("when you remove the last ... counter"). A counter of
 * {@link CounterType#ANY} kind comes off the one kind the source has enough of, or the kind its controller chooses
 * among several: the chosen kind's removal is spliced in as the next step and records the step.
 */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromSourceEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromSourceEffect) effect;
        List<Permanent> permanents = subjectResolutionService.permanents(gameData, entry, effect, e.subject());
        if (e.counterType() == CounterType.ANY) {
            removeCounterOfAnyKind(gameData, entry, e, permanents);
            return;
        }
        // A REMOVE_COUNTERS step: "remove a counter from it. If you do" reads how many came off (never prompts).
        Map<UUID, Integer> before = permanentCounterSupport.counterCounts(gameData,
                permanents.stream().map(Permanent::getId).toList(), e.counterType());
        for (Permanent permanent : permanents) {
            int amount = e.dynamicAmount() != null
                    ? amountEvaluationService.evaluate(gameData, e.dynamicAmount(),
                    AmountContext.forStackEntry(entry, permanent))
                    : e.amount();
            int countBefore = permanent.getCounterCount(e.counterType());
            permanentCounterSupport.removeCounterFromPermanent(gameData, permanent, e.counterType(), amount);
            permanentCounterSupport.notifyLastCounterRemoved(gameData, permanent, e.counterType(),
                    countBefore - permanent.getCounterCount(e.counterType()), entry.getControllerId());
        }
        entry.recordStep(StepKind.REMOVE_COUNTERS,
                permanentCounterSupport.counterChange(gameData, before, e.counterType(), false));
    }

    private void removeCounterOfAnyKind(GameData gameData, StackEntry entry, RemoveCounterFromSourceEffect e,
                                        List<Permanent> permanents) {
        entry.recordStep(StepKind.REMOVE_COUNTERS, StepOutcome.NONE);
        if (permanents.isEmpty()) {
            return;
        }
        List<CounterType> kinds = PermanentCounterSupport.removableCounterKinds(permanents.getFirst(), e.amount());
        if (kinds.size() == 1) {
            resolve(gameData, entry, withKind(e, kinds.getFirst()));
        } else if (kinds.size() > 1) {
            var options = kinds.stream()
                    .map(kind -> new ChooseOneEffect.ChooseOneOption(
                            "Remove " + e.amount() + " " + permanentCounterSupport.counterTypeName(kind) + " counters",
                            withKind(e, kind)))
                    .toList();
            playerInputService.beginChooseModeChoice(gameData, entry.getControllerId(), entry.getCard(),
                    new ChooseOneEffect(options), entry.getSourcePermanentId());
        }
    }

    private static RemoveCounterFromSourceEffect withKind(RemoveCounterFromSourceEffect e, CounterType kind) {
        return new RemoveCounterFromSourceEffect(kind, e.amount(), null, false, e.subject());
    }
}
