package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import java.util.Set;
import java.util.HashSet;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromPlayersEffect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromOpponentsUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantProtectionFromOpponentsUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromOpponentsUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (!entry.getControllerId().equals(targetControllerId)) {
            return;
        }

        Set<UUID> opponentIds = new HashSet<>();
        for (UUID playerId : gameData.playerIds) {
            if (!playerId.equals(entry.getControllerId())) {
                opponentIds.add(playerId);
            }
        }
        gameData.addEffectOnPermanent(entry.getCard().getName(), null, entry.getControllerId(),
                new GrantEffectEffect(new ProtectionFromPlayersEffect(opponentIds, true), GrantScope.TARGET), target.getId(),
                com.github.laxika.magicalvibes.model.effect.Duration.UNTIL_END_OF_TURN);

        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(" gains protection from each of its controller's opponents until end of turn.")
                .build());
    }
}
