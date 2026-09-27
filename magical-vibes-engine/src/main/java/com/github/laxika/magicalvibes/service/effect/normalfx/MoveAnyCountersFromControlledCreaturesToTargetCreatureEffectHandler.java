package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveAnyCountersFromControlledCreaturesToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** Resolves the counter-moving half of Slippery Bogbonder's enter-the-battlefield ability. */
@Component
@RequiredArgsConstructor
public class MoveAnyCountersFromControlledCreaturesToTargetCreatureEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveAnyCountersFromControlledCreaturesToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)
                || gameQueryService.cantHaveCounters(gameData, target)) {
            return;
        }

        List<ChoiceContext.CounterSource> sources = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields
                .getOrDefault(entry.getControllerId(), List.of())) {
            if (permanent.getId().equals(target.getId()) || !gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            permanent.getCounters().forEach((counterType, count) -> {
                if (count > 0 && counterType != CounterType.ANY && counterType != CounterType.SILVER) {
                    sources.add(new ChoiceContext.CounterSource(permanent.getId(), counterType,
                            permanent.getCard().getName()));
                }
            });
        }

        beginNextChoice(gameData, entry, sources, 0, target);
    }

    private void beginNextChoice(GameData gameData, StackEntry entry,
                                 List<ChoiceContext.CounterSource> sources, int index, Permanent target) {
        while (index < sources.size()) {
            ChoiceContext.CounterSource source = sources.get(index);
            Permanent from = gameQueryService.findPermanentById(gameData, source.permanentId());
            if (from != null && gameQueryService.isCreature(gameData, from)
                    && from.getCounterCount(source.counterType()) > 0
                    && !cantHaveCounter(gameData, target, source.counterType())) {
                playerInputService.beginMoveAnyCountersFromControlledCreaturesAmountChoice(
                        gameData, entry.getControllerId(), sources, index, target.getId(),
                        entry.getCard().getName(), from.getCounterCount(source.counterType()));
                return;
            }
            index++;
        }
    }

    private boolean cantHaveCounter(GameData gameData, Permanent permanent, CounterType counterType) {
        if (gameQueryService.cantHaveCounters(gameData, permanent)) {
            return true;
        }
        return switch (counterType) {
            case MINUS_ONE_MINUS_ONE -> gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent);
            case PLUS_ONE_PLUS_ONE -> gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent);
            default -> false;
        };
    }
}
