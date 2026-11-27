package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PlayerCounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfEachKindToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AddAnotherCounterOfEachKindToTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddAnotherCounterOfEachKindToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = !targetIds.isEmpty() ? targetIds.getFirst() : entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent permanent = gameQueryService.findPermanentById(gameData, targetId);
        if (permanent != null) {
            addCountersToPermanent(gameData, entry, permanent);
        } else if (gameData.playerIdToName.containsKey(targetId)) {
            addCountersToPlayer(gameData, entry, targetId);
        }
    }

    private void addCountersToPermanent(GameData gameData, StackEntry entry, Permanent target) {
        List<CounterType> counterTypes = new ArrayList<>();
        for (CounterType counterType : CounterType.values()) {
            if (counterType != CounterType.ANY && counterType != CounterType.SILVER
                    && target.getCounterCount(counterType) > 0) {
                counterTypes.add(counterType);
            }
        }

        for (CounterType counterType : counterTypes) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, counterType, 1);
        }
    }

    private void addCountersToPlayer(GameData gameData, StackEntry entry, UUID playerId) {
        for (PlayerCounterType kind : PlayerCounterType.values()) {
            if (gameData.playerCounterCount(playerId, kind) > 0) {
                lifeSupport.givePlayerCounters(gameData, playerId, kind, 1, entry.getCard().getName(),
                        entry.getControllerId());
            }
        }
    }
}
