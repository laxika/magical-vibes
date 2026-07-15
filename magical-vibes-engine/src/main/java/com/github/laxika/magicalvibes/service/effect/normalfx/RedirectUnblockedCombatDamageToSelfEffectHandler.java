package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectUnblockedCombatDamageToSelfEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectUnblockedCombatDamageToSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                List<Permanent> bf = gameData.playerBattlefields.get(entry.getControllerId());
                if (bf == null) return;
                for (Permanent p : bf) {
                    if (p.getCard() == entry.getCard()) {
                        gameData.combatDamageRedirectTarget = p.getId();

                        String logEntry = p.getCard().getName() + "'s ability resolves — unblocked combat damage will be redirected to it this turn.";
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
                        log.info("Game {} - Combat damage redirect set to {}", gameData.id, p.getCard().getName());
                        return;
                    }
                }
    
    }
}
