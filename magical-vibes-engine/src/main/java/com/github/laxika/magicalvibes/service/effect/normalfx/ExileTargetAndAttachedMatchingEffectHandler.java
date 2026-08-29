package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetAndAttachedMatchingEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
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
 * Resolves {@link ExileTargetAndAttachedMatchingEffect} by exiling matching attachments first,
 * then the targeted creature.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetAndAttachedMatchingEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetAndAttachedMatchingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileTargetAndAttachedMatchingEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        List<Permanent> targets = new ArrayList<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                targets.add(target);
            }
        }
        if (targets.isEmpty()) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        List<Permanent> matchingAttached = new ArrayList<>();
        for (Permanent target : targets) {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    if (target.getId().equals(permanent.getAttachedTo())
                            && predicateEvaluationService.matchesPermanentPredicate(
                                    permanent, exile.attachedFilter(), filterContext)) {
                        matchingAttached.add(permanent);
                    }
                }
            }
        }

        for (Permanent attached : matchingAttached) {
            exileToExile(gameData, entry, attached);
        }
        for (Permanent target : targets) {
            exileToExile(gameData, entry, target);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private void exileToExile(GameData gameData, StackEntry entry, Permanent permanent) {
        if (permanentRemovalService.removePermanentToExile(gameData, permanent)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(permanent.getCard(), " is exiled."));
            log.info("Game {} - {} is exiled by {}",
                    gameData.id, permanent.getCard().getName(), entry.getCard().getName());
        }
    }
}
