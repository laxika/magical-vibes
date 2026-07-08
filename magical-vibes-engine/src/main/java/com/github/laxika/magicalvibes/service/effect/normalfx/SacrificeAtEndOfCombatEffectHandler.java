package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SacrificeAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (self != null) {
                    gameData.queueDelayedAction(new SacrificeAtEndOfCombat(self.getId()));
                    String logEntry = entry.getCard().getName() + " will be sacrificed at end of combat.";
                    gameData.gameLog.add(logEntry);
                }
    
    }
}
