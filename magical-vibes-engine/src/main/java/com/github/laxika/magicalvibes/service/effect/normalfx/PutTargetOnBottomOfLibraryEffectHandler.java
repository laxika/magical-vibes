package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetOnBottomOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetOnBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) return;

                if (permanentRemovalService.removePermanentToLibraryBottom(gameData, target)) {
                    String logEntry = target.getCard().getName() + " is put on the bottom of its owner's library.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                    log.info("Game {} - {} put on bottom of library", gameData.id, target.getCard().getName());
                }

                permanentRemovalService.removeOrphanedAuras(gameData);
    
    }
}
