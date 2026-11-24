package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PlayerCounterType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
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
 * "Remove up to N / any number of / N [kind] counters from [permanents or players]" ({@link
 * RemoveUpToCountersFromAllPermanentsEffect}): one "how many" question per permanent or player and kind, as removal
 * slots of a {@link ChoiceContext.MoveCountersAmountChoice}, bounded by what is there now and by what the total
 * leaves (CR 608.2d); for "remove N counters" each question's lowest answer is what the later questions can no longer
 * make up, and a question with one possible answer is answered unasked. The counters all come off once the last
 * question is answered, and the REMOVE_COUNTERS step is recorded then, before the resolution goes on to its next
 * effect; with nothing to remove it is recorded at once.
 */
@Component
@RequiredArgsConstructor
public class RemoveUpToCountersFromAllPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PlayerInputService playerInputService;
    private final BattleDefeatSupport battleDefeatSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveUpToCountersFromAllPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var remove = (RemoveUpToCountersFromAllPermanentsEffect) effect;
        entry.recordStep(StepKind.REMOVE_COUNTERS, StepOutcome.NONE);
        List<ChoiceContext.CounterMoveSlot> slots = new ArrayList<>();
        if (remove.permanents() != null) {
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    remove.permanents())) {
                List<CounterType> kinds = remove.counterType() == CounterType.ANY
                        ? PermanentCounterSupport.counterKindsOn(permanent) : List.of(remove.counterType());
                for (CounterType kind : kinds) {
                    if (permanent.getCounterCount(kind) > 0) {
                        slots.add(new ChoiceContext.CounterMoveSlot(permanent.getId(), null, kind));
                    }
                }
            }
        }
        if (remove.players() != null && remove.counterType() == CounterType.ANY) {
            for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, remove.players())) {
                for (PlayerCounterType kind : PlayerCounterType.values()) {
                    if (gameData.playerCounterCount(playerId, kind) > 0) {
                        slots.add(ChoiceContext.CounterMoveSlot.fromPlayer(playerId, kind));
                    }
                }
            }
        }
        int totalLimit = Math.max(0, amountEvaluationService.evaluate(gameData, remove.amount(),
                AmountContext.forStackEntry(entry, sourceOf(gameData, entry))));
        if (slots.isEmpty() || totalLimit == 0) {
            return;
        }
        int available = 0;
        for (ChoiceContext.CounterMoveSlot slot : slots) {
            available = Math.min(Integer.MAX_VALUE - 1, available + countOn(gameData, slot));
        }
        int requiredTotal = remove.exactly() ? Math.min(totalLimit, available) : 0;
        askNext(gameData, entry, entry.getControllerId(), slots, 0, List.of(),
                FaceDownVisibility.sourceName(gameData, entry), totalLimit, requiredTotal);
    }

    /** Refuses an answer to the removal slot {@code ctx} asked that is outside what the question offered. */
    public void checkAnswer(GameData gameData, ChoiceContext.MoveCountersAmountChoice ctx, int chosen) {
        int answeredTotal = ctx.answeredTotal();
        int min = minimumFor(gameData, ctx.slots(), ctx.index(), answeredTotal, ctx.requiredTotal());
        int max = maximumFor(gameData, ctx.slots().get(ctx.index()), answeredTotal, ctx.totalLimit());
        if (chosen < min || chosen > max) {
            throw new IllegalArgumentException("Choose between " + min + " and " + max + " counters");
        }
    }

    /**
     * Applies the answer ({@link #checkAnswer checked}) to the removal slot {@code ctx} asked, then asks the next slot
     * or removes everything answered. Returns whether another slot was asked.
     */
    public boolean answer(GameData gameData, StackEntry entry, UUID controllerId,
                          ChoiceContext.MoveCountersAmountChoice ctx, int chosen) {
        List<Integer> answered = new ArrayList<>(ctx.moved());
        answered.add(chosen);
        return askNext(gameData, entry, controllerId, ctx.slots(), ctx.index() + 1, answered,
                ctx.sourceCardName(), ctx.totalLimit(), ctx.requiredTotal());
    }

    private boolean askNext(GameData gameData, StackEntry entry, UUID controllerId,
                            List<ChoiceContext.CounterMoveSlot> slots, int index, List<Integer> answered,
                            String sourceName, int totalLimit, int requiredTotal) {
        List<Integer> moved = new ArrayList<>(answered);
        for (int i = index; i < slots.size(); i++) {
            int answeredTotal = moved.stream().mapToInt(Integer::intValue).sum();
            ChoiceContext.CounterMoveSlot slot = slots.get(i);
            int max = maximumFor(gameData, slot, answeredTotal, totalLimit);
            int min = minimumFor(gameData, slots, i, answeredTotal, requiredTotal);
            if (max <= 0 || min >= max) {
                moved.add(Math.max(0, max));
                continue;
            }
            playerInputService.beginMoveCountersAmountChoice(gameData, controllerId,
                    new ChoiceContext.MoveCountersAmountChoice(slots, i, moved, sourceName, totalLimit, requiredTotal),
                    slotOwnerName(gameData, slot), null, counterName(slot), min, max);
            return true;
        }
        removeAnswered(gameData, entry, slots, moved);
        return false;
    }

    private Permanent sourceOf(GameData gameData, StackEntry entry) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        return source != null ? source : entry.getSourcePermanentSnapshot();
    }

    /** The most the slot may remove: what is there now, and no more than the total leaves. */
    private int maximumFor(GameData gameData, ChoiceContext.CounterMoveSlot slot, int answeredTotal, int totalLimit) {
        return Math.max(0, Math.min(countOn(gameData, slot), totalLimit - answeredTotal));
    }

    /** The least the slot must remove so the later slots can still make up what "remove N counters" requires. */
    private int minimumFor(GameData gameData, List<ChoiceContext.CounterMoveSlot> slots, int index, int answeredTotal,
                           int requiredTotal) {
        int later = 0;
        for (int i = index + 1; i < slots.size(); i++) {
            later = Math.min(Integer.MAX_VALUE - 1, later + countOn(gameData, slots.get(i)));
        }
        return Math.max(0, requiredTotal - answeredTotal - later);
    }

    private int countOn(GameData gameData, ChoiceContext.CounterMoveSlot slot) {
        if (slot.isPlayerSlot()) {
            return gameData.playerCounterCount(slot.fromPlayerId(), slot.playerCounterType());
        }
        Permanent permanent = gameQueryService.findPermanentById(gameData, slot.fromPermanentId());
        return permanent == null ? 0 : permanent.getCounterCount(slot.counterType());
    }

    private String slotOwnerName(GameData gameData, ChoiceContext.CounterMoveSlot slot) {
        if (slot.isPlayerSlot()) {
            return gameData.playerIdToName.get(slot.fromPlayerId());
        }
        return FaceDownVisibility.publicName(gameData,
                gameQueryService.findPermanentById(gameData, slot.fromPermanentId()));
    }

    private String counterName(ChoiceContext.CounterMoveSlot slot) {
        return slot.isPlayerSlot() ? slot.playerCounterType().name().toLowerCase()
                : permanentCounterSupport.counterTypeName(slot.counterType());
    }

    /**
     * Removes what each answered slot chose, together, with what a removal of those counters sends (the oil and
     * last-counter events, vanishing's last time counter, a battle's last defense counter), and records the
     * REMOVE_COUNTERS step: the total, the permanents that lost counters, their controllers and the players that lost
     * counters.
     */
    private void removeAnswered(GameData gameData, StackEntry entry, List<ChoiceContext.CounterMoveSlot> slots,
                                List<Integer> answered) {
        Set<UUID> permanentIds = new LinkedHashSet<>();
        slots.stream().filter(slot -> !slot.isPlayerSlot()).forEach(slot -> permanentIds.add(slot.fromPermanentId()));
        Map<UUID, Integer> before = permanentCounterSupport.counterCounts(gameData, permanentIds, null);
        int playerTotal = 0;
        List<UUID> playersLosing = new ArrayList<>();
        for (int i = 0; i < answered.size() && i < slots.size(); i++) {
            int count = answered.get(i);
            ChoiceContext.CounterMoveSlot slot = slots.get(i);
            if (count <= 0) {
                continue;
            }
            if (slot.isPlayerSlot()) {
                int removed = gameData.removePlayerCounters(slot.fromPlayerId(), slot.playerCounterType(), count);
                if (removed > 0) {
                    playerTotal += removed;
                    if (!playersLosing.contains(slot.fromPlayerId())) {
                        playersLosing.add(slot.fromPlayerId());
                    }
                    gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(slot.fromPlayerId())
                            + " loses " + removed + " " + counterName(slot) + " counter(s)."));
                }
                continue;
            }
            Permanent permanent = gameQueryService.findPermanentById(gameData, slot.fromPermanentId());
            if (permanent == null) {
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
        StepOutcome fromPermanents = permanentCounterSupport.counterChange(gameData, before, null, false);
        if (playerTotal == 0) {
            entry.recordStep(StepKind.REMOVE_COUNTERS, fromPermanents);
            return;
        }
        List<UUID> players = new ArrayList<>(fromPermanents.playerIds());
        playersLosing.stream().filter(id -> !players.contains(id)).forEach(players::add);
        entry.recordStep(StepKind.REMOVE_COUNTERS, new StepOutcome(fromPermanents.amount() + playerTotal,
                fromPermanents.objectIds(), players));
    }
}
