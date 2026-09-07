package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnBatch;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerChoosesCardFromGraveyardToBattlefieldEffect;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerChoosesCardFromGraveyardToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerChoosesCardFromGraveyardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var chooseEffect = (EachPlayerChoosesCardFromGraveyardToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        gameData.pendingGraveyardReturnBatch = new PendingGraveyardReturnBatch(
                controllerId, java.util.List.of(), java.util.Map.of());
        for (UUID playerId : gameData.orderedPlayerIds) {
            gameData.pendingGraveyardReturnQueue.add(new PendingGraveyardReturnChoice(
                    playerId, 1, chooseEffect.filter(), GraveyardChoiceDestination.BATTLEFIELD,
                    false, true, false, false, Set.of(), Set.of()));
        }
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }
}
