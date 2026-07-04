package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentDiscardsEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachOpponentDiscardsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentDiscardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOpponentDiscardsEffect) effect;

        UUID controllerId = entry.getControllerId();
        // Build APNAP-ordered queue with only opponents (skip controller)
        List<UUID> choosers = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        if (!activePlayerId.equals(controllerId)) {
            choosers.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId) && !playerId.equals(controllerId)) {
                choosers.add(playerId);
            }
        }
        playerInteractionSupport.startNextEachPlayerDiscard(gameData,
                DiscardFollowUp.eachPlayer(choosers, controllerId, e.amount()));
    
    }
}
