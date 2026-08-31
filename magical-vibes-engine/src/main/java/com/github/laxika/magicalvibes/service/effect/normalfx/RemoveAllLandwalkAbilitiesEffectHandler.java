package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LandwalkIgnoredForBlockingEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllLandwalkAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
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
public class RemoveAllLandwalkAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAllLandwalkAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        for (Keyword landwalk : Keyword.LANDWALK_MAP.keySet()) {
            target.getRemovedKeywords().add(landwalk);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                    new RemoveKeywordEffect(landwalk, GrantScope.TARGET), targetId, null, null,
                    EffectDuration.UNTIL_END_OF_TURN, 0));
        }

        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                new LandwalkIgnoredForBlockingEffect(), targetId, null, null,
                EffectDuration.UNTIL_END_OF_TURN, 0));

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " loses all landwalk abilities until end of turn."));
        log.info("Game {} - {} loses all landwalk abilities until end of turn",
                gameData.id, target.getCard().getName());
    }
}
