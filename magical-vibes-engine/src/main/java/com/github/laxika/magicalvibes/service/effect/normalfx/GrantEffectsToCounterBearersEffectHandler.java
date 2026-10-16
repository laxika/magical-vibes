package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCantBlockMatchingCreaturesEffect;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Installs source-independent continuous effects keyed to a counter on the affected permanent. */
@Component
public class GrantEffectsToCounterBearersEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantEffectsToCounterBearersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var rule = (GrantEffectsToCounterBearersEffect) effect;
        PermanentPredicate counterScope = new PermanentHasCountersPredicate(rule.counterType());
        PermanentPredicate scope = counterScope;
        PermanentPredicate boundPredicate = null;
        if (rule.boundToTargetCard()) {
            UUID targetCardId = entry.getTargetCardIds() != null && !entry.getTargetCardIds().isEmpty()
                    ? entry.getTargetCardIds().getFirst()
                    : entry.getTargetId();
            Permanent bound = targetCardId == null ? null : findPermanentOfCard(gameData, targetCardId);
            if (bound == null || bound.getCounterCount(rule.counterType()) <= 0) {
                return;
            }
            boundPredicate = new PermanentIsSpecificPermanentPredicate(bound.getId());
            scope = new PermanentAllOfPredicate(List.of(boundPredicate, counterScope));
        } else if (ruleAlreadyEstablished(gameData, rule, scope)) {
            return;
        }

        String sourceName = entry.getCard() != null ? entry.getCard().getName() : null;
        UUID controllerId = entry.getControllerId();
        for (CardEffect declaredEffect : rule.effects()) {
            CardEffect grantedEffect = boundPredicate == null
                    ? declaredEffect : narrowedToBoundPermanent(declaredEffect, boundPredicate);
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), sourceName, null,
                    controllerId, grantedEffect, null, null, scope, EffectDuration.PERMANENT, 0,
                    rule.boundToTargetCard()));
        }
    }

    /** Restricts the effects that carry their own affected-creature predicate to the bound permanent. */
    private static CardEffect narrowedToBoundPermanent(CardEffect effect, PermanentPredicate bound) {
        if (effect instanceof GoadCreaturesUntilNextTurnEffect goad) {
            return new GoadCreaturesUntilNextTurnEffect(
                    new PermanentAllOfPredicate(List.of(bound, goad.affectedPredicate())));
        }
        if (effect instanceof MatchingCreaturesCantBlockMatchingCreaturesEffect restriction) {
            return new MatchingCreaturesCantBlockMatchingCreaturesEffect(
                    new PermanentAllOfPredicate(List.of(bound, restriction.blockerPredicate())),
                    restriction.attackerPredicate(), restriction.description());
        }
        return effect;
    }

    private static Permanent findPermanentOfCard(GameData gameData, UUID cardId) {
        List<Permanent> found = new ArrayList<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (permanent.getCard().getId().equals(cardId)) {
                found.add(permanent);
            }
        });
        return found.isEmpty() ? null : found.getFirst();
    }

    private boolean ruleAlreadyEstablished(GameData gameData, GrantEffectsToCounterBearersEffect rule,
                                           PermanentPredicate scope) {
        synchronized (gameData.floatingEffects) {
            return rule.effects().stream().allMatch(grantedEffect -> gameData.floatingEffects.stream()
                    .anyMatch(floating -> scope.equals(floating.scope())
                            && grantedEffect.equals(floating.effect())));
        }
    }
}
