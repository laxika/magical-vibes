package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AdjustEachCounterKindOnTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AdjustEachCounterKindOnTargetEffect} (Quarry Hauler). Snapshots every distinct
 * counter kind present on the target as the ability resolves, then hands the controller the per-kind
 * add/remove decisions through the generic list-choice interaction
 * ({@code ChoiceContext.AdjustCounterKindChoice}). A target with no counters (or gone from the
 * battlefield) does nothing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdjustEachCounterKindOnTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AdjustEachCounterKindOnTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = !targetIds.isEmpty() ? targetIds.getFirst() : entry.getTargetId();
        if (targetId == null) {
            log.info("Game {} - {}: target no longer present, effect fizzles", gameData.id, entry.getCard().getName());
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            log.info("Game {} - {}: target no longer on battlefield, effect fizzles", gameData.id, entry.getCard().getName());
            return;
        }

        // Snapshot the distinct counter kinds present now (ANY/SILVER are wildcard categories, never
        // stored on a permanent). One add/remove decision follows per kind.
        List<CounterType> kinds = new ArrayList<>();
        for (CounterType type : CounterType.values()) {
            if (type == CounterType.ANY || type == CounterType.SILVER) {
                continue;
            }
            if (target.getCounterCount(type) > 0) {
                kinds.add(type);
            }
        }

        if (kinds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardTextCard(entry.getCard(), " finds no counters on ", target.getCard(), "."));
            log.info("Game {} - {}: target has no counters", gameData.id, entry.getCard().getName());
            return;
        }

        playerInputService.beginAdjustCounterKindChoice(gameData, entry.getControllerId(), targetId,
                entry.getCard().getName(), kinds);
    }
}
