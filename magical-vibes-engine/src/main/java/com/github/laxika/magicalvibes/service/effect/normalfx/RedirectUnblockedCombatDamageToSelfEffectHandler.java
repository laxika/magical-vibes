package com.github.laxika.magicalvibes.service.effect.normalfx;

import java.util.UUID;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectUnblockedCombatDamageToSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectUnblockedCombatDamageToSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) return;
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), FaceDownVisibility.publicName(gameData, source),
                source.getId(), entry.getControllerId(), (RedirectUnblockedCombatDamageToSelfEffect) effect,
                source.getId(), entry.getControllerId(), null, Duration.UNTIL_END_OF_TURN, 0));
        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                "'s ability resolves — unblocked combat damage will be redirected to it this turn."));
        log.info("Game {} - Combat damage redirect set to {}", gameData.id, source.getCard().getName());
    }
}
