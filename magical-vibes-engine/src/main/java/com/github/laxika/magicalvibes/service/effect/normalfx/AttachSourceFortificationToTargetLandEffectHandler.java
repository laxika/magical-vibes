package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachSourceFortificationToTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachSourceFortificationToTargetLandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachSourceFortificationToTargetLandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null
                || !GameQueryService.permanentHasSubtype(source, CardSubtype.FORTIFICATION)
                || !gameQueryService.isLand(gameData, target)
                || (gameQueryService.isCreature(gameData, source)
                && !gameQueryService.isLand(gameData, source))) {
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(source.getId());
        source.setAttachedTo(target.getId());
        source.setTimestamp(gameData.nextTimestamp());

        gameLogService.append(gameData,
                GameLog.cardTextCard(source.getCard(), " is now attached to ", target.getCard(), "."));
        log.info("Game {} - {} attached to {}", gameData.id, source.getCard().getName(), target.getCard().getName());
    }
}
