package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves a draw for the player the trigger was about: the current controller of its triggering permanent, else that
 * permanent's last known controller (CR 608.2h), else the player the trigger recorded (the attacking player of
 * "whenever a player attacks"); failing those, the source's controller or the entry's player target.
 */
@Component
@RequiredArgsConstructor
public class DrawCardForTriggeringPlayerEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardForTriggeringPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawCardForTriggeringPlayerEffect) effect;
        UUID triggeringPlayerId = gameQueryService.findTriggeringPermanentController(gameData, entry);
        UUID playerId = triggeringPlayerId != null
                ? triggeringPlayerId
                : entry.getSourcePermanentId() != null
                        ? gameQueryService.findPermanentController(gameData, entry.getSourcePermanentId())
                : entry.getTargetId();
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        for (int i = 0; i < amount; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }
    }
}
