package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegenerateEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegenerateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                UUID regenerationTargetId = entry.getTargetId();
                if (regenerationTargetId == null && entry.getSourcePermanentId() != null) {
                    Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                    if (source != null) {
                        regenerationTargetId = source.getAttachedTo();
                    }
                }

                Permanent perm = gameQueryService.findPermanentById(gameData, regenerationTargetId);
                if (perm == null) {
                    return;
                }
                perm.setRegenerationShield(perm.getRegenerationShield() + 1);

                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(perm.getCard(), " gains a regeneration shield."));
                log.info("Game {} - {} gains a regeneration shield", gameData.id, perm.getCard().getName());
    
    }
}
