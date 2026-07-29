package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Ether Well: put the targeted creature on top of its owner's library, unless it matches the
 * effect's condition — then the controller is offered the bottom of the library instead, and the
 * top placement only happens if they decline (completed by the mayfx handler).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        if (predicateEvaluationService.matchesPermanentPredicate(gameData, target, e.bottomOptionCondition())) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    entry.getControllerId(),
                    List.of(e),
                    entry.getCard().getName() + " — Put " + target.getCard().getName()
                            + " on the bottom of its owner's library instead?",
                    target.getId()
            ));
            return;
        }

        putOnTop(gameData, target, entry.getCard().getName());
    }

    /** Shared with the mayfx handler's decline branch: the default top-of-library placement. */
    public void putOnTop(GameData gameData, Permanent target, String sourceName) {
        if (permanentRemovalService.removePermanentToLibraryTop(gameData, target)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(target.getCard(), " is put on top of its owner's library."));
            log.info("Game {} - {} put on top of library by {}", gameData.id, target.getCard().getName(), sourceName);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
