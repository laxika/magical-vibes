package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantSubtypeToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSubtypeToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantSubtypeToTargetCreatureEffect) effect;
        
                Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (target == null) return;

                if (!target.getGrantedSubtypes().contains(e.subtype())) {
                    target.getGrantedSubtypes().add(e.subtype());
                    String subtypeLog = target.getCard().getName() + " becomes a " + e.subtype().getDisplayName() + " in addition to its other types.";
                    gameBroadcastService.logAndBroadcast(gameData, subtypeLog);
                }
    
    }
}
