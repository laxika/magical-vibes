package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesAndPutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GoadTargetCreatureUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a mass goad and counts the creatures affected by that resolution. */
@Component
@RequiredArgsConstructor
public class GoadCreaturesAndPutCountersOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoadCreaturesAndPutCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var goadAndCount = (GoadCreaturesAndPutCountersOnSourceEffect) effect;
        List<Permanent> candidates = new ArrayList<>();
        gameData.forEachPermanent((ignoredControllerId, permanent) -> candidates.add(permanent));

        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        FilterContext context = FilterContext.of(gameData)
                .withSourceControllerId(entry.getControllerId())
                .withSourceCardId(entry.getCard() == null ? null : entry.getCard().getId())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withSourcePermanentSnapshot(source);

        int goadedCount = 0;
        for (Permanent candidate : candidates) {
            if (!gameQueryService.isCreature(gameData, candidate)
                    || !predicateEvaluationService.matchesPermanentPredicate(
                    candidate, goadAndCount.affectedPredicate(), context)) {
                continue;
            }
            goadedCount++;
            if (isAlreadyGoadedBy(gameData, candidate.getId(), entry.getControllerId())) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard() == null ? "Goad" : entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(),
                    new GoadTargetCreatureUntilNextTurnEffect(), candidate.getId(),
                    null, null, EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
        }

        if (goadedCount > 0 && source != null) {
            permanentCounterSupport.placeCounterOnPermanent(
                    gameData, entry, source, goadAndCount.counterType(), goadedCount);
        }
    }

    /**
     * Returns whether the given player already goads the creature. A player goading a creature again
     * has no effect and creates no additional combat requirement (CR 701.15d).
     */
    private boolean isAlreadyGoadedBy(GameData gameData, UUID creatureId, UUID goadingPlayerId) {
        return gameData.floatingEffects.stream()
                .anyMatch(effect -> effect.effect() instanceof GoadTargetCreatureUntilNextTurnEffect
                        && creatureId.equals(effect.affectedPermanentId())
                        && goadingPlayerId.equals(effect.controllerId()));
    }
}
