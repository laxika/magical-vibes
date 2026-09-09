package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetCreaturePowerToughnessEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoubleTargetCreaturePowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleTargetCreaturePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        Permanent target = targetId == null
                ? null
                : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        int currentPower = gameQueryService.getEffectivePower(gameData, target);
        int currentToughness = gameQueryService.getEffectiveToughness(gameData, target);
        target.setPowerModifier(target.getPowerModifier() + currentPower);
        target.setToughnessModifier(target.getToughnessModifier() + currentToughness);

        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text("'s power and toughness are doubled (+" + currentPower + "/+" + currentToughness + ").")
                .build());
        log.info("Game {} - {} power/toughness doubled (+{}/+{})", gameData.id,
                target.getCard().getName(), currentPower, currentToughness);
    }
}
