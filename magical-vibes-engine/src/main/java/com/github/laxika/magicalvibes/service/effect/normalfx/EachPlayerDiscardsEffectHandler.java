package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EachPlayerDiscardsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerDiscardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerDiscardsEffect) effect;

        UUID controllerId = entry.getControllerId();
        // Build APNAP-ordered queue: active player first, then others in turn order
        List<UUID> choosers = new ArrayList<>();
        UUID activePlayerId = gameData.activePlayerId;
        choosers.add(activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(activePlayerId)) {
                choosers.add(playerId);
            }
        }
        // Start the first player's discard; the queue remainder rides the discard choice
        playerInteractionSupport.startNextEachPlayerDiscard(gameData,
                DiscardFollowUp.eachPlayer(choosers, controllerId, e.amount()));
    
    }
}
