package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTargetPermanentToHandOrLibraryTopByPredicateEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetPermanentToHandOrLibraryTopByPredicateEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        boolean toLibraryTop = predicateEvaluationService.matchesPermanentPredicate(gameData, target, e.libraryTopCondition());

        if (toLibraryTop) {
            if (permanentRemovalService.removePermanentToLibraryTop(gameData, target)) {
                String logEntry = target.getCard().getName() + " is put on top of its owner's library.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} put on top of library by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());
            }
        } else {
            if (permanentRemovalService.removePermanentToHand(gameData, target)) {
                String logEntry = target.getCard().getName() + " is returned to its owner's hand.";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} returned to owner's hand by {}", gameData.id, target.getCard().getName(), entry.getCard().getName());
            }
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
