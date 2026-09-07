package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AdjustChosenCounterOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Clockspinning's choice of a counter on a permanent or suspended card. */
@Component
@RequiredArgsConstructor
public class AdjustChosenCounterOnTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AdjustChosenCounterOnTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = !targetIds.isEmpty() ? targetIds.getFirst() : entry.getTargetId();
        if (targetId == null) {
            return;
        }

        if (entry.getTargetZone() == Zone.EXILE) {
            Integer timeCounters = gameData.exiledCardTimeCounters.get(targetId);
            if (gameData.findExiledCard(targetId) != null && timeCounters != null && timeCounters > 0) {
                playerInputService.beginAdjustChosenCounterTypeChoice(
                        gameData, entry.getControllerId(), targetId, Zone.EXILE,
                        entry.getCard().getName(), List.of(CounterType.TIME));
            }
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        List<CounterType> counterTypes = new ArrayList<>();
        if (effect instanceof AdjustChosenCounterOnTargetEffect adjustEffect
                && adjustEffect.fixedCounterType() != null) {
            if (target.getCounterCount(adjustEffect.fixedCounterType()) > 0) {
                counterTypes.add(adjustEffect.fixedCounterType());
            }
        } else {
            for (CounterType counterType : CounterType.values()) {
                if (counterType != CounterType.ANY && counterType != CounterType.SILVER
                        && target.getCounterCount(counterType) > 0) {
                    counterTypes.add(counterType);
                }
            }
        }
        if (!counterTypes.isEmpty()) {
            playerInputService.beginAdjustChosenCounterTypeChoice(
                    gameData, entry.getControllerId(), targetId, null,
                    entry.getCard().getName(), counterTypes);
        }
    }
}
