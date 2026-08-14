package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetToSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTargetToSourcePermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AttachTargetToSourcePermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) return;

        if (GameQueryService.permanentHasSubtype(target, CardSubtype.EQUIPMENT)
                && gameQueryService.cantBeEquipped(gameData, source)) {
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(target.getId());
        target.setAttachedTo(source.getId());
        // CR 613.7e: an attachment receives a new timestamp each time it becomes attached.
        target.setTimestamp(gameData.nextTimestamp());

        gameLogService.append(gameData, GameLog.cardTextCard(target.getCard(), " is attached to ", source.getCard(), "."));
        log.info("Game {} - {} attached to {}", gameData.id, target.getCard().getName(), source.getCard().getName());
    }
}
