package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.service.effect.HandlesEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExileResolutionService {

    private final GameHelper gameHelper;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @HandlesEffect(ExileTargetPermanentEffect.class)
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

    @HandlesEffect(ExileSelfAndReturnAtEndStepEffect.class)
    void resolveExileSelfAndReturnAtEndStep(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        Card card = source.getOriginalCard();
        gameHelper.removePermanentToExile(gameData, source);

        String logEntry = card.getName() + " is exiled. It will return at the beginning of the next end step.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} is exiled and will return at next end step",
                gameData.id, card.getName());

        gameData.pendingExileReturns.add(new PendingExileReturn(card, entry.getControllerId()));

        gameHelper.removeOrphanedAuras(gameData);
    }
}

