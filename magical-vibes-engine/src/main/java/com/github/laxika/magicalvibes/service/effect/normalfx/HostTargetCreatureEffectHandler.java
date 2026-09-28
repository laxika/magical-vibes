package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HostTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HostTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HostTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        Permanent realm = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (target == null || realm == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        target.addHostedByRealm(realm.getId());
        gameLogService.append(gameData, GameLog.cardTextCard(
                realm.getCard(), " hosts ", target.getCard(), "."));
        log.info("Game {} - {} hosts {}", gameData.id, realm.getCard().getName(), target.getCard().getName());
    }
}
