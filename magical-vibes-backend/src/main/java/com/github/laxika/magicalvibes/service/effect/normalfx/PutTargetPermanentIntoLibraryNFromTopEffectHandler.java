package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetPermanentIntoLibraryNFromTopEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetPermanentIntoLibraryNFromTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetPermanentIntoLibraryNFromTopEffect) effect;
        
                Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) return;

                int position = e.position();
                if (permanentRemovalService.removePermanentToLibraryPosition(gameData, target, position)) {
                    String ordinal = switch (position) {
                        case 0 -> "on top of";
                        case 1 -> "second from the top of";
                        case 2 -> "third from the top of";
                        default -> (position + 1) + "th from the top of";
                    };
                    String logEntry = target.getCard().getName() + " is put " + ordinal + " its owner's library.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} put {} library (position {})", gameData.id, target.getCard().getName(), ordinal, position);
                }

                permanentRemovalService.removeOrphanedAuras(gameData);
    
    }
}
