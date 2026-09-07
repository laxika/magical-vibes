package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyUpToOneAttachedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a resolution-time choice to destroy at most one attached permanent. */
@Component
@RequiredArgsConstructor
public class DestroyUpToOneAttachedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyUpToOneAttachedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroy = (DestroyUpToOneAttachedPermanentEffect) effect;
        UUID targetCreatureId = entry.getTargetId();
        Permanent targetCreature = gameQueryService.findPermanentById(gameData, targetCreatureId);
        if (targetCreature == null) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        List<UUID> eligibleIds = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (targetCreatureId.equals(permanent.getAttachedTo())
                        && predicateEvaluationService.matchesPermanentPredicate(
                        permanent, destroy.attachedFilter(), filterContext)) {
                    eligibleIds.add(permanent.getId());
                }
            }
        });

        if (!eligibleIds.isEmpty()) {
            playerInputService.beginMultiPermanentChoice(
                    gameData,
                    entry.getControllerId(),
                    eligibleIds,
                    1,
                    new MultiPermanentChoiceContext.DestroyUpToOneAttachedPermanent(
                            targetCreatureId,
                            destroy.attachedFilter(),
                            entry.getCard().getId(),
                            entry.getControllerId(),
                            entry.getCard().getName()),
                    "Choose up to one attached permanent to destroy.");
        }
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.DestroyUpToOneAttachedPermanent context) {
        if (permanentIds.isEmpty()) {
            return;
        }

        Permanent targetCreature = gameQueryService.findPermanentById(gameData, context.targetCreatureId());
        Permanent attached = gameQueryService.findPermanentById(gameData, permanentIds.getFirst());
        if (targetCreature == null || attached == null
                || !context.targetCreatureId().equals(attached.getAttachedTo())) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(context.sourceCardId())
                .withSourceControllerId(context.sourceControllerId());
        if (predicateEvaluationService.matchesPermanentPredicate(
                attached, context.attachedFilter(), filterContext)) {
            destructionSupport.tryDestroyAndLog(gameData, attached, context.sourceCardName());
        }
    }
}
