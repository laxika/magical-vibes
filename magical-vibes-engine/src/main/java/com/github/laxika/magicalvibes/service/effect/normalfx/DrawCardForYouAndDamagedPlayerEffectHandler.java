package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForYouAndDamagedPlayerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Resolves "you and that player each draw": the controller and the damaged player draw in turn
 * order starting with the active player (CR 608.2f).
 */
@Component
@RequiredArgsConstructor
public class DrawCardForYouAndDamagedPlayerEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardForYouAndDamagedPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        if (damagedPlayerId == null || !gameData.playerIds.contains(damagedPlayerId)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData,
                ((DrawCardForYouAndDamagedPlayerEffect) effect).amount(), AmountContext.forStackEntry(entry, source));

        List<UUID> drawingPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = drawingPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) Collections.rotate(drawingPlayers, -activeIndex);
        for (UUID playerId : drawingPlayers) {
            if (playerId.equals(entry.getControllerId()) || playerId.equals(damagedPlayerId)) {
                playerInteractionSupport.applyDrawCards(gameData, playerId, amount);
            }
        }
    }
}
