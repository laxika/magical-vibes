package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.EffectHandlerProvider;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExileResolutionService implements EffectHandlerProvider {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(ExileTargetPermanentEffect.class,
                (gd, entry, effect) -> resolveExileTargetPermanent(gd, entry));
    }

    void resolveExileTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        gameHelper.removePermanentToExile(gameData, target);
        String logEntry = target.getCard().getName() + " is exiled.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is exiled by {}'s ability",
                gameData.id, target.getCard().getName(), entry.getCard().getName());

        gameHelper.removeOrphanedAuras(gameData);
    }
}

