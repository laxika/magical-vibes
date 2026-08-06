package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PutCounterOnReferencedPermanentEffect}: resolves the referenced permanent,
 * checks the optional condition against it, then hands it to
 * {@link PermanentCounterSupport#placeCounterOnPermanent}, which owns {@code cantHaveCounters},
 * +1/+1 doubling, -1/-1 reduction and the counter-placement triggers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutCounterOnReferencedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnReferencedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterOnReferencedPermanentEffect e = (PutCounterOnReferencedPermanentEffect) effect;
        String sourceName = entry.getCard() != null ? entry.getCard().getName() : "Source";

        Permanent referenced = switch (e.reference()) {
            case ATTACHED -> findAttached(gameData, entry, sourceName);
            case TRIGGERING -> findPermanent(gameData, entry.getTriggeringPermanentId());
        };
        if (referenced == null) {
            return;
        }

        if (e.condition() != null
                && !predicateEvaluationService.matchesPermanentPredicate(gameData, referenced, e.condition())) {
            log.info("Game {} - {}: referenced permanent does not match the counter condition", gameData.id, sourceName);
            return;
        }

        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, referenced, e.counterType(), e.count());
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }

    private Permanent findAttached(GameData gameData, StackEntry entry, String sourceName) {
        Permanent source = findPermanent(gameData, entry.getSourcePermanentId());
        if (source == null || !source.isAttached()) {
            log.info("Game {} - {} fizzles: source no longer attached", gameData.id, sourceName);
            return null;
        }

        Permanent host = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        if (host == null) {
            log.info("Game {} - {} fizzles: attached permanent no longer on battlefield", gameData.id, sourceName);
        }
        return host;
    }
}
