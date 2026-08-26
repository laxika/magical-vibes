package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
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
    private final AmountEvaluationService amountEvaluationService;

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
            // Unreachable: the record's constructor rejects SOURCE (PutCountersOnSourceEffect owns it).
            case SOURCE -> throw new IllegalStateException("SOURCE counters belong on PutCountersOnSourceEffect");
            case RETURNED -> findPermanentByCardId(gameData, entry.getTargetId());
        };
        if (referenced == null) {
            return;
        }

        if (e.condition() != null
                && !predicateEvaluationService.matchesPermanentPredicate(gameData, referenced, e.condition())) {
            log.info("Game {} - {}: referenced permanent does not match the counter condition", gameData.id, sourceName);
            return;
        }

        int count = amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, referenced));
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, referenced, e.counterType(), count);
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        return permanentId == null ? null : gameQueryService.findPermanentById(gameData, permanentId);
    }

    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        return gameData.playerBattlefields.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(java.util.Collection::stream)
                .filter(permanent -> cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId())))
                .findFirst()
                .orElse(null);
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
