package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnBatch;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CloseGraveyardReturnBatchEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CloseGraveyardReturnBatchEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CloseGraveyardReturnBatchEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PendingGraveyardReturnBatch batch = gameData.pendingGraveyardReturnBatch;
        if (batch == null) {
            return;
        }
        if (batch.cards().isEmpty()) {
            gameData.pendingGraveyardReturnBatch = null;
            return;
        }
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }
}
