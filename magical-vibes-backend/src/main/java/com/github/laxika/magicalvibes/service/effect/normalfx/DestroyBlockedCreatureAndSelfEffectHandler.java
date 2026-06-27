package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DestroyBlockedCreatureAndSelfEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyBlockedCreatureAndSelfEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent attacker = gameQueryService.findPermanentById(gameData, entry.getTargetId());
                if (attacker != null) {
                    destructionSupport.tryDestroyAndLog(gameData, attacker, entry.getCard().getName());
                }

                Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (self != null) {
                    destructionSupport.tryDestroyAndLog(gameData, self, entry.getCard().getName());
                }
    }
}
