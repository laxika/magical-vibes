package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.YouAndTargetPlayerEachDrawCardEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves "you and target player each draw": the controller and the target draw in turn order. */
@Component
@RequiredArgsConstructor
public class YouAndTargetPlayerEachDrawCardEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return YouAndTargetPlayerEachDrawCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (YouAndTargetPlayerEachDrawCardEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<UUID> drawingPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = drawingPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) Collections.rotate(drawingPlayers, -activeIndex);
        for (UUID playerId : drawingPlayers) {
            if (playerId.equals(entry.getControllerId()) || playerId.equals(targetPlayerId)) {
                playerInteractionSupport.applyDrawCards(gameData, playerId, e.amount());
            }
        }
    }
}
