package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RemoveUpToCountersFromTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveUpToCountersFromTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveUpToCountersFromTargetEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = targetIds.isEmpty() ? entry.getTargetId() : targetIds.getFirst();
        if (targetId == null) {
            return;
        }

        List<String> counterKinds = counterKinds(gameData, targetId, e.permanentPredicate());
        if (counterKinds.isEmpty()) {
            return;
        }

        int available = counterKinds.stream()
                .mapToInt(counterKind -> counterCount(gameData, targetId, counterKind))
                .sum();
        int remaining = Math.min(Math.max(0, e.maxAmount()), available);
        int maxForCurrentKind = Math.min(remaining, counterCount(gameData, targetId, counterKinds.getFirst()));
        playerInputService.beginRemoveCountersOfKindChoice(gameData, entry.getControllerId(), targetId,
                entry.getCard().getName(), counterKinds, 0, remaining, maxForCurrentKind);
    }

    private List<String> counterKinds(GameData gameData, UUID targetId,
                                      com.github.laxika.magicalvibes.model.filter.PermanentPredicate predicate) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            List<String> kinds = new ArrayList<>();
            for (CounterType counterType : CounterType.values()) {
                if (counterType != CounterType.ANY && counterType != CounterType.SILVER
                        && target.getCounterCount(counterType) > 0) {
                    kinds.add(counterType.name());
                }
            }
            return kinds;
        }

        List<String> kinds = new ArrayList<>();
        if (gameData.playerPoisonCounters.getOrDefault(targetId, 0) > 0) {
            kinds.add("POISON");
        }
        if (gameData.playerEnergyCounters.getOrDefault(targetId, 0) > 0) {
            kinds.add("ENERGY");
        }
        return kinds;
    }

    private int counterCount(GameData gameData, UUID targetId, String counterKind) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            return target.getCounterCount(CounterType.valueOf(counterKind));
        }
        return switch (counterKind) {
            case "POISON" -> gameData.playerPoisonCounters.getOrDefault(targetId, 0);
            case "ENERGY" -> gameData.playerEnergyCounters.getOrDefault(targetId, 0);
            default -> 0;
        };
    }
}
