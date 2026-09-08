package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerDiscardsEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves fixed-count discards for every selected target player in APNAP order. */
@Component
@RequiredArgsConstructor
public class EachTargetPlayerDiscardsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachTargetPlayerDiscardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachTargetPlayerDiscardsEffect) effect;
        gameData.lastDiscardedCardManaValue = 0;
        List<UUID> remainingTargets = new ArrayList<>(entry.getTargetIds());
        List<UUID> choosers = new ArrayList<>(remainingTargets.size());

        UUID activePlayerId = gameData.activePlayerId;
        if (activePlayerId != null && remainingTargets.remove(activePlayerId)) {
            choosers.add(activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (remainingTargets.remove(playerId)) {
                choosers.add(playerId);
            }
        }
        choosers.addAll(remainingTargets);

        playerInteractionSupport.startNextEachPlayerDiscard(gameData,
                DiscardFollowUp.eachPlayer(choosers, entry.getControllerId(), e.amount()));
    }
}
