package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * "Remove up to N / any number of [kind] counters from among [permanents]" ({@link
 * RemoveUpToCountersFromAllPermanentsEffect}): one "how many" question per permanent and kind, as removal slots of a
 * {@link ChoiceContext.MoveCountersAmountChoice}, bounded by what the permanent has now and by what the total leaves
 * (CR 608.2d). The counters all come off once the last question is answered, and the REMOVE_COUNTERS step is recorded
 * then, before the resolution goes on to its next effect; with nothing to remove it is recorded at once.
 */
@Component
@RequiredArgsConstructor
public class RemoveUpToCountersFromAllPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;
    private final BattleDefeatSupport battleDefeatSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveUpToCountersFromAllPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var remove = (RemoveUpToCountersFromAllPermanentsEffect) effect;
        entry.recordStep(StepKind.REMOVE_COUNTERS, StepOutcome.NONE);
        List<ChoiceContext.CounterMoveSlot> slots = new ArrayList<>();
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, remove.permanents())) {
            List<CounterType> kinds = remove.counterType() == CounterType.ANY
                    ? PermanentCounterSupport.counterKindsOn(permanent) : List.of(remove.counterType());
            for (CounterType kind : kinds) {
                if (permanent.getCounterCount(kind) > 0) {
                    slots.add(new ChoiceContext.CounterMoveSlot(permanent.getId(), null, kind));
                }
            }
        }
        if (slots.isEmpty() || remove.maxAmount() == 0) {
            return;
        }
        askNext(gameData, entry, entry.getControllerId(), slots, 0, List.of(),
                FaceDownVisibility.sourceName(gameData, entry), remove.maxAmount());
    }

    /**
     * Applies the answer to the removal slot {@code ctx} asked (at most what the permanent has now and what the total
     * leaves), then asks the next slot or removes everything answered. Returns whether another slot was asked.
     */
    public boolean answer(GameData gameData, StackEntry entry, UUID controllerId,
                          ChoiceContext.MoveCountersAmountChoice ctx, int chosen) {
        ChoiceContext.CounterMoveSlot slot = ctx.slots().get(ctx.index());
        Permanent from = gameQueryService.findPermanentById(gameData, slot.fromPermanentId());
        int available = from == null ? 0 : from.getCounterCount(slot.counterType());
        int counted = Math.max(0, Math.min(chosen, Math.min(available, ctx.totalLimit() - ctx.answeredTotal())));
        List<Integer> answered = new ArrayList<>(ctx.moved());
        answered.add(counted);
        return askNext(gameData, entry, controllerId, ctx.slots(), ctx.index() + 1, answered,
                ctx.sourceCardName(), ctx.totalLimit());
    }

    private boolean askNext(GameData gameData, StackEntry entry, UUID controllerId,
                            List<ChoiceContext.CounterMoveSlot> slots, int index, List<Integer> answered,
                            String sourceName, int totalLimit) {
        int left = totalLimit - answered.stream().mapToInt(Integer::intValue).sum();
        for (int i = index; i < slots.size() && left > 0; i++) {
            ChoiceContext.CounterMoveSlot slot = slots.get(i);
            Permanent from = gameQueryService.findPermanentById(gameData, slot.fromPermanentId());
            if (from == null || from.getCounterCount(slot.counterType()) <= 0) {
                continue;
            }
            List<Integer> moved = new ArrayList<>(answered);
            while (moved.size() < i) {
                moved.add(0);
            }
            playerInputService.beginMoveCountersAmountChoice(gameData, controllerId,
                    new ChoiceContext.MoveCountersAmountChoice(slots, i, moved, sourceName, totalLimit),
                    FaceDownVisibility.publicName(gameData, from), null,
                    permanentCounterSupport.counterTypeName(slot.counterType()),
                    Math.min(left, from.getCounterCount(slot.counterType())));
            return true;
        }
        removeAnswered(gameData, entry, slots, answered);
        return false;
    }

    /**
     * Removes what each answered slot chose, together, with what a removal of those counters sends (the oil and
     * last-counter events, vanishing's last time counter, a battle's last defense counter), and records the
     * REMOVE_COUNTERS step: the total, the permanents that lost counters and their controllers.
     */
    private void removeAnswered(GameData gameData, StackEntry entry, List<ChoiceContext.CounterMoveSlot> slots,
                                List<Integer> answered) {
        Set<UUID> permanentIds = new LinkedHashSet<>();
        slots.forEach(slot -> permanentIds.add(slot.fromPermanentId()));
        Map<UUID, Integer> before = permanentCounterSupport.counterCounts(gameData, permanentIds, null);
        for (int i = 0; i < answered.size() && i < slots.size(); i++) {
            int count = answered.get(i);
            ChoiceContext.CounterMoveSlot slot = slots.get(i);
            Permanent permanent = gameQueryService.findPermanentById(gameData, slot.fromPermanentId());
            if (count <= 0 || permanent == null) {
                continue;
            }
            int countBefore = permanent.getCounterCount(slot.counterType());
            permanentCounterSupport.removeCounterFromPermanent(gameData, permanent, slot.counterType(), count);
            permanentCounterSupport.notifyLastCounterRemoved(gameData, permanent, slot.counterType(),
                    countBefore - permanent.getCounterCount(slot.counterType()), entry.getControllerId());
            if (slot.counterType() == CounterType.DEFENSE) {
                battleDefeatSupport.checkAfterDefenseRemoved(gameData, permanent);
            }
        }
        entry.recordStep(StepKind.REMOVE_COUNTERS, permanentCounterSupport.counterChange(gameData, before, null, false));
    }
}
