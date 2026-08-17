package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.SwitchPowerToughnessEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SwitchPowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SwitchPowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var switchEffect = (SwitchPowerToughnessEffect) effect;
        List<UUID> targetIds;
        if (switchEffect.self()) {
            targetIds = entry.getSourcePermanentId() == null
                    ? List.of()
                    : List.of(entry.getSourcePermanentId());
        } else {
            targetIds = entry.targetsForEffect(switchEffect);
            if (targetIds.isEmpty() && entry.getTargetId() != null) {
                targetIds = List.of(entry.getTargetId());
            }
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(),
                    switchEffect, target.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

            gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                    "'s power and toughness are switched until end of turn."));

            log.info("Game {} - {}'s power and toughness switched", gameData.id, target.getCard().getName());
        }
    }
}
