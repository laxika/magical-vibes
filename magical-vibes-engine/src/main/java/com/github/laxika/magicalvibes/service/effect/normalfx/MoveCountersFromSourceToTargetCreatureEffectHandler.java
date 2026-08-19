package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MoveCountersFromSourceToTargetCreatureEffect} by asking how many counters to
 * move, then letting the shared input completion path place them on the target.
 */
@Component
@RequiredArgsConstructor
public class MoveCountersFromSourceToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCountersFromSourceToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MoveCountersFromSourceToTargetCreatureEffect move =
                (MoveCountersFromSourceToTargetCreatureEffect) effect;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }

        int available = source.getCounterCount(move.counterType());
        if (available <= 0) {
            return;
        }

        playerInputService.beginMoveCountersAmountChoice(
                gameData,
                entry.getControllerId(),
                source.getId(),
                target.getId(),
                move.counterType(),
                entry.getCard().getName(),
                available);
    }
}
