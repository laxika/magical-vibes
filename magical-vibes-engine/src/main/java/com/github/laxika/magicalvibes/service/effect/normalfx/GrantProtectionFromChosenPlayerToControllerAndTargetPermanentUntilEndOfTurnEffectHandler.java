package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromChosenPlayerToControllerAndTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GrantProtectionFromChosenPlayerToControllerAndTargetPermanentUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantProtectionFromChosenPlayerToControllerAndTargetPermanentUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null && entry.getSourcePermanentSnapshot() != null) {
            source = new Permanent(entry.getSourcePermanentSnapshot());
        }
        if (source == null) {
            return;
        }

        UUID chosenPlayerId = source.getProtectionFromPlayerIdsPermanently().stream()
                .findFirst().orElse(null);
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (chosenPlayerId == null || target == null
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, target.getId()))) {
            return;
        }

        target.getProtectionFromPlayerIdsUntilEndOfTurn().add(chosenPlayerId);
        gameData.playerProtectionFromPlayerIdsUntilEndOfTurn
                .computeIfAbsent(entry.getControllerId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(chosenPlayerId);

        gameLogService.append(gameData, GameLog.cardThen(target.getCard(),
                " and " + gameData.playerIdToName.get(entry.getControllerId())
                        + " gain protection from " + gameData.playerIdToName.get(chosenPlayerId)
                        + " until end of turn."));
    }
}
