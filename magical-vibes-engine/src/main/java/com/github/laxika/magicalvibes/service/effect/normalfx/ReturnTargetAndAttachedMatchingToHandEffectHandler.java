package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetAndAttachedMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ReturnTargetAndAttachedMatchingToHandEffect}: bounce matching attachments
 * first, then the targeted permanent, so filtered Auras return to hand instead of dying as
 * orphans.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnTargetAndAttachedMatchingToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameBroadcastService gameBroadcastService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetAndAttachedMatchingToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetAndAttachedMatchingToHandEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        List<Permanent> matchingAttached = new ArrayList<>();
        UUID targetId = target.getId();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (permanent.isAttached()
                    && targetId.equals(permanent.getAttachedTo())
                    && predicateEvaluationService.matchesPermanentPredicate(
                            permanent, e.attachedFilter(), filterContext)) {
                matchingAttached.add(permanent);
            }
        });

        for (Permanent attached : matchingAttached) {
            bounceToHand(gameData, entry, attached);
        }
        bounceToHand(gameData, entry, target);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void bounceToHand(GameData gameData, StackEntry entry, Permanent permanent) {
        if (permanentRemovalService.removePermanentToHand(gameData, permanent)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(permanent.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}",
                    gameData.id, permanent.getCard().getName(), entry.getCard().getName());
        }
    }
}
