package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSelfToHandAndCreateTokensEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSelfToHandAndCreateTokensEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSelfToHandAndCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnSelfToHandAndCreateTokensEffect) effect;
        
                // Try to return source to hand; if it already left the battlefield, skip the bounce
                // but still create tokens — the token creation is not contingent on the return.
                Permanent toReturn = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (toReturn != null) {
                    permanentRemovalService.removePermanentToHand(gameData, toReturn);
                    permanentRemovalService.removeOrphanedAuras(gameData);

                    String logEntry = entry.getCard().getName() + " is returned to its owner's hand.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                    log.info("Game {} - {} returned to hand", gameData.id, entry.getCard().getName());
                } else {
                    String logEntry = entry.getCard().getName() + " is no longer on the battlefield.";
                    gameBroadcastService.logAndBroadcast(gameData, logEntry);
                }

                permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), e.tokenEffect(), entry.getCard().getSetCode());
    
    }
}
