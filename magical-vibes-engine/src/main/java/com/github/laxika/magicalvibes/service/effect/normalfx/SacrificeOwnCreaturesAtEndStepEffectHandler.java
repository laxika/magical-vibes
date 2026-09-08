package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOwnCreaturesAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Captures matching creatures at resolution and queues their individual sacrifice actions for the
 * next end step.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeOwnCreaturesAtEndStepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeOwnCreaturesAtEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeOwnCreaturesAtEndStepEffect) effect;
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());
        List<UUID> creatureIds = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && predicateEvaluationService.matchesPermanentPredicate(
                            permanent, e.filter(), filterContext)) {
                creatureIds.add(permanent.getId());
            }
        }

        for (UUID creatureId : creatureIds) {
            gameData.queueDelayedAction(new DelayedPermanentAction(
                    creatureId, DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
        }
        log.info("Game {} - {} creature(s) scheduled for sacrifice at end step by {}",
                gameData.id, creatureIds.size(), entry.getCard().getName());
    }
}
