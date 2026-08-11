package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorsUntilYourNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantProtectionFromColorsUntilYourNextTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromColorsUntilYourNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantProtectionFromColorsUntilYourNextTurnEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                new ProtectionFromColorsEffect(grant.colors(), GrantScope.TARGET), target.getId(), null, null,
                EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));

        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(" gains protection from each color until your next turn.")
                .build());
        log.info("Game {} - {} gains protection from each color until your next turn",
                gameData.id, target.getCard().getName());
    }
}
