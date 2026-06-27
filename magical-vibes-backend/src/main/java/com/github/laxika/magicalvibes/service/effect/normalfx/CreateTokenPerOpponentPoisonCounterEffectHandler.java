package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerOpponentPoisonCounterEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenPerOpponentPoisonCounterEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenPerOpponentPoisonCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokenPerOpponentPoisonCounterEffect) effect;
        
                UUID controllerId = entry.getControllerId();

                // Count total poison counters on all opponents
                int totalPoison = 0;
                for (UUID playerId : gameData.orderedPlayerIds) {
                    if (!playerId.equals(controllerId)) {
                        totalPoison += gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                    }
                }

                if (totalPoison == 0) {
                    log.info("Game {} - No poison counters on opponents, no tokens created", gameData.id);
                    return;
                }

                CreateTokenEffect tokenEffect = new CreateTokenEffect(
                        totalPoison, e.tokenName(), e.power(), e.toughness(),
                        e.color(), e.subtypes(), e.keywords(), e.additionalTypes()
                );
                permanentControlSupport.applyCreateToken(gameData, controllerId, tokenEffect, entry.getCard().getSetCode());
    
    }
}
