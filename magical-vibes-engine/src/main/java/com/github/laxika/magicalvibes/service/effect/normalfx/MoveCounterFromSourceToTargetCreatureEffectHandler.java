package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves {@link MoveCounterFromSourceToTargetCreatureEffect}: removes counters from the permanent they move from and
 * puts them on the other, put by the entry's controller. Nothing moves when either left the battlefield, both are one
 * permanent, there is no such counter, or the counter can't be put on (CR 122.5). A choice of kind or of how many is
 * made as the effect resolves (CR 608.2d): a kind is asked only when several can move, and "any number" asks one
 * question per permanent and kind, in {@code from}'s, then {@code to}'s, then counter-kind order.
 */
@Component
@RequiredArgsConstructor
public class MoveCounterFromSourceToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCounterFromSourceToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MoveCounterFromSourceToTargetCreatureEffect move = (MoveCounterFromSourceToTargetCreatureEffect) effect;
        switch (move.quantity()) {
            case AMOUNT -> moveAmount(gameData, entry, move);
            case ALL -> moveAll(gameData, entry, move);
            case ANY_NUMBER -> askAnyNumber(gameData, entry, move);
        }
    }

    private void moveAmount(GameData gameData, StackEntry entry, MoveCounterFromSourceToTargetCreatureEffect move) {
        Permanent from = subjectResolutionService.permanent(gameData, entry, move, move.from());
        Permanent to = subjectResolutionService.permanent(gameData, entry, move, move.to());
        if (from == null || to == null || from.getId().equals(to.getId())) {
            return;
        }
        if (move.counterType() == CounterType.ANY) {
            List<CounterType> kinds = PermanentCounterSupport.counterKindsOn(from).stream()
                    .filter(kind -> permanentCounterSupport.canReceiveCounter(gameData, to, kind))
                    .toList();
            if (kinds.size() == 1) {
                permanentCounterSupport.moveCounters(gameData, entry, from, to, kinds.getFirst(), 1);
            } else if (kinds.size() > 1) {
                playerInputService.beginMoveOneCounterChoice(gameData, entry.getControllerId(), from.getId(),
                        to.getId(), entry.getCard().getName(), kinds);
            }
            return;
        }
        if (from.getCounterCount(move.counterType()) <= 0) {
            return;
        }
        Permanent source = subjectResolutionService.permanent(gameData, entry, move, Subjects.source());
        int amount = amountEvaluationService.evaluate(gameData, move.amount(),
                AmountContext.forStackEntry(entry, source));
        permanentCounterSupport.moveCounters(gameData, entry, from, to, move.counterType(), amount);
    }

    private void moveAll(GameData gameData, StackEntry entry, MoveCounterFromSourceToTargetCreatureEffect move) {
        Permanent from = subjectResolutionService.permanent(gameData, entry, move, move.from());
        Permanent to = subjectResolutionService.permanent(gameData, entry, move, move.to());
        if (from == null || to == null || from.getId().equals(to.getId())) {
            return;
        }
        List<CounterType> kinds = move.counterType() == CounterType.ANY
                ? PermanentCounterSupport.counterKindsOn(from) : List.of(move.counterType());
        List<ChoiceContext.CounterMoveSlot> slots = new ArrayList<>();
        List<Integer> moved = new ArrayList<>();
        for (CounterType kind : kinds) {
            slots.add(new ChoiceContext.CounterMoveSlot(from.getId(), to.getId(), kind));
            moved.add(permanentCounterSupport.removeCountersForMove(gameData, from, to, kind,
                    from.getCounterCount(kind), entry.getControllerId()));
        }
        permanentCounterSupport.askNextCounterMove(gameData, entry, entry.getControllerId(), slots, slots.size(),
                moved, entry.getCard().getName());
    }

    private void askAnyNumber(GameData gameData, StackEntry entry, MoveCounterFromSourceToTargetCreatureEffect move) {
        List<Permanent> froms = subjectResolutionService.permanents(gameData, entry, move, move.from());
        List<Permanent> tos = subjectResolutionService.permanents(gameData, entry, move, move.to());
        List<ChoiceContext.CounterMoveSlot> slots = new ArrayList<>();
        for (Permanent from : froms) {
            List<CounterType> kinds = move.counterType() == CounterType.ANY
                    ? PermanentCounterSupport.counterKindsOn(from) : List.of(move.counterType());
            for (Permanent to : tos) {
                if (from.getId().equals(to.getId())) {
                    continue;
                }
                for (CounterType kind : kinds) {
                    slots.add(new ChoiceContext.CounterMoveSlot(from.getId(), to.getId(), kind));
                }
            }
        }
        permanentCounterSupport.askNextCounterMove(gameData, entry, entry.getControllerId(), slots, 0, List.of(),
                FaceDownVisibility.sourceName(gameData, entry));
    }
}
