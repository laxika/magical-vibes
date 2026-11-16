package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterAndWatchTargetCreatureDeathEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Puts the counter on each target and creates the delayed triggered ability about it (CR 603.7a): "when that creature
 * dies, if it has [that counter] on it, …" (Xira, the Golden Sting), which has no stated duration (CR 603.7b). Whether
 * the creature had the counter is judged as it last existed on the battlefield (CR 603.10a).
 */
@Component
@RequiredArgsConstructor
public class PutCounterAndWatchTargetCreatureDeathEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterAndWatchTargetCreatureDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterAndWatchTargetCreatureDeathEffect watched =
                (PutCounterAndWatchTargetCreatureDeathEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !predicateEvaluationService.matchesPermanentPredicate(
                    target, watched.targetRestriction(), FilterContext.of(gameData)
                            .withSourceControllerId(entry.getControllerId())
                            .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                            .withSourcePermanentId(entry.getSourcePermanentId()))) {
                continue;
            }

            int placed = permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, target, watched.counterType(), 1);
            if (placed > 0) {
                gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.about(
                        entry.getControllerId(), entry.getCard(),
                        new TriggeredAbility(Triggers.dies(new PermanentHasCountersPredicate(watched.counterType()),
                                ObjectRelation.THIS), watched.deathEffect()),
                        target.getId(), entry.getSourcePermanentId(), false));
            }
        }
    }
}
