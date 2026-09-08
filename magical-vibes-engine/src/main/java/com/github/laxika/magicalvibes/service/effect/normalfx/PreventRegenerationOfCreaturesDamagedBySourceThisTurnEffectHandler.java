package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventRegenerationOfCreaturesDamagedBySourceThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreventRegenerationOfCreaturesDamagedBySourceThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventRegenerationOfCreaturesDamagedBySourceThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getTargetId() != null
                ? entry.getTargetId()
                : entry.getSourcePermanentId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null || !gameQueryService.isCreature(gameData, source)) {
            return;
        }

        source.setDamagedCreaturesCantRegenerateThisTurn(true);

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                ": creatures it dealt damage to this turn can't be regenerated this turn."));
        log.info("Game {} - creatures damaged by {} can't be regenerated this turn", gameData.id,
                source.getCard().getName());
    }
}
