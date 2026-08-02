package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LicidEndEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link LicidEndEffect}: the Licid detaches and reverts to its printed creature form.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LicidEndEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LicidEndEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !source.getCard().isAura()) {
            return;
        }

        source.setAttachedTo(null);
        gameData.expireFloatingEffectsForUnattachedSource(source.getId());
        source.setCard(source.getOriginalCard());
        source.setTimestamp(gameData.nextTimestamp());

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                " stops being an Aura and becomes a creature again."));
        log.info("Game {} - {} reverts from Aura to creature", gameData.id, source.getCard().getName());
    }
}
