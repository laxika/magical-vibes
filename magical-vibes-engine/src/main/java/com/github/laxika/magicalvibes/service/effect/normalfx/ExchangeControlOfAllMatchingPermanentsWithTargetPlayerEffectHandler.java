package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves a mass control exchange by collecting both sides before applying any control changes.
 */
@Component
@RequiredArgsConstructor
public class ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffectHandler
        implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exchange = (ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)
                || controllerId.equals(targetPlayerId)) {
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
        List<Permanent> controlledPermanents = matchingPermanents(gameData, controllerId, exchange, filterContext);
        List<Permanent> targetPermanents = matchingPermanents(gameData, targetPlayerId, exchange, filterContext);
        GainControlOfTargetEffect controlEffect = new GainControlOfTargetEffect(exchange.duration());

        for (Permanent permanent : controlledPermanents) {
            creatureControlService.applyControlEffect(gameData, targetPlayerId, permanent,
                    controlEffect, exchange.duration().toEffectDuration(), null, entry.getCard().getName());
        }
        for (Permanent permanent : targetPermanents) {
            creatureControlService.applyControlEffect(gameData, controllerId, permanent,
                    controlEffect, exchange.duration().toEffectDuration(), null, entry.getCard().getName());
        }
    }

    private List<Permanent> matchingPermanents(GameData gameData, UUID playerId,
                                               ExchangeControlOfAllMatchingPermanentsWithTargetPlayerEffect exchange,
                                               FilterContext filterContext) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }

        List<Permanent> matching = new ArrayList<>();
        for (Permanent permanent : List.copyOf(battlefield)) {
            if (exchange.filter() == null
                    || predicateEvaluationService.matchesPermanentPredicate(permanent, exchange.filter(), filterContext)) {
                matching.add(permanent);
            }
        }
        return matching;
    }
}
