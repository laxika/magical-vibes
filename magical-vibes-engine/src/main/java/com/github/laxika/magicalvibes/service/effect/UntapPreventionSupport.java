package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapLockCondition;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.List;

/** Resolves self-scoped untap locks, including locks gated by a static condition. */
@Component
@RequiredArgsConstructor
public class UntapPreventionSupport {

    private final ConditionEvaluationService conditionEvaluationService;
    @Autowired
    private GameQueryService gameQueryService;
    @Autowired
    private PredicateEvaluationService predicateEvaluationService;

    public boolean hasActiveSelfDoesntUntap(GameData gameData, Permanent permanent) {
        UUID controllerId = gameData.findControllerOf(permanent);
        return gameData.floatingEffects.stream().anyMatch(floating ->
                permanent.getId().equals(floating.affectedPermanentId())
                        && floating.effect() instanceof DoesntUntapEffect lock
                        && (floating.scope() == null || predicateEvaluationService.matchesPermanentPredicate(
                                permanent, floating.scope(), FilterContext.of(gameData)))
                        && (lock.condition() == UntapLockCondition.ALWAYS
                                || lock.condition() == UntapLockCondition.WHILE_SOURCE_CONTROLLED
                                && gameData.playerBattlefields.getOrDefault(floating.controllerId(), List.of())
                                .stream().anyMatch(source -> source.getId().equals(floating.sourcePermanentId()))))
                || !(gameQueryService == null
                        ? permanent.isFaceDown() || permanent.isLosesAllAbilitiesUntilEndOfTurn()
                        : gameQueryService.hasLostPrintedAbilities(gameData, permanent))
                && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(effect -> hasActiveSelfDoesntUntap(gameData, permanent, controllerId, effect));
    }

    private boolean hasActiveSelfDoesntUntap(GameData gameData, Permanent permanent, UUID controllerId,
                                              CardEffect effect) {
        if (effect instanceof DoesntUntapEffect doesNotUntap) {
            return doesNotUntap.scope() == TapUntapScope.SELF;
        }
        if (effect instanceof ConditionalEffect conditional) {
            return conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(permanent, controllerId))
                    && hasActiveSelfDoesntUntap(gameData, permanent, controllerId, conditional.wrapped());
        }
        return false;
    }
}
