package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromControlledPermanentsToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MoveCountersFromControlledPermanentsToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCountersFromControlledPermanentsToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }

        MoveCountersFromControlledPermanentsToSourceEffect move =
                (MoveCountersFromControlledPermanentsToSourceEffect) effect;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        List<UUID> candidates = gameData.playerBattlefields
                .getOrDefault(entry.getControllerId(), List.of())
                .stream()
                .filter(permanent -> move.includeSource() || !permanent.getId().equals(sourceId))
                .filter(permanent -> move.filter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(permanent, move.filter(), filterContext))
                .filter(permanent -> permanent.getCounterCount(move.counterType()) > 0)
                .map(Permanent::getId)
                .toList();

        beginNextChoice(gameData, entry, move.counterType(), move.countersPerMovedCounter(), candidates, 0, sourceId);
    }

    private void beginNextChoice(GameData gameData, StackEntry entry,
                                 com.github.laxika.magicalvibes.model.CounterType counterType,
                                 int countersPerMovedCounter, List<UUID> candidates, int index, UUID sourceId) {
        while (index < candidates.size()) {
            Permanent from = gameQueryService.findPermanentById(gameData, candidates.get(index));
            if (from != null && from.getCounterCount(counterType) > 0) {
                playerInputService.beginMoveCountersFromControlledPermanentsAmountChoice(
                        gameData, entry.getControllerId(), candidates, index, sourceId, counterType,
                        entry.getCard().getName(), from.getCard().getName(), from.getCounterCount(counterType),
                        countersPerMovedCounter);
                return;
            }
            index++;
        }
    }
}
