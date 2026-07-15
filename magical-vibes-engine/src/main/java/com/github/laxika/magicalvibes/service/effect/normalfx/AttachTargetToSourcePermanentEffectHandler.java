package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AttachTargetToSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachTargetToSourcePermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

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

                gameData.expireFloatingEffectsForUnattachedSource(target.getId());
                target.setAttachedTo(source.getId());
                // CR 613.7e: an attachment receives a new timestamp each time it becomes attached.
                target.setTimestamp(gameData.nextTimestamp());
                String attachLog = target.getCard().getName() + " is attached to " + source.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(attachLog));
                log.info("Game {} - {} attached to {}", gameData.id, target.getCard().getName(), source.getCard().getName());
    
    }
}
