package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.UntapLockCondition;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.List;

/**
 * Resolves the untap locks a permanent has of its own (a {@code source()} {@link DoesntUntapEffect}, including one gated
 * by a static condition) and the resolved locks on it.
 */
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
                        ? permanent.isFaceDown() || gameData.losesAllAbilitiesByEffect(permanent)
                        : gameQueryService.hasLostPrintedAbilities(gameData, permanent))
                && permanent.getCard().getStaticEffects().stream()
                .anyMatch(effect -> hasActiveSelfDoesntUntap(gameData, permanent, controllerId, effect));
    }

    /**
     * Whether a resolved "it doesn't untap during its controller's untap step for as long as [its source] remains on
     * the battlefield / remains tapped" effect is on {@code permanent} (Somnophore, Rust Tick, Kill Switch). The
     * duration clock ends each as its source leaves, phases out or untaps, so one in force applies.
     */
    public boolean hasSourceLinkedUntapLock(GameData gameData, Permanent permanent) {
        return gameData.floatingEffectsOnPermanent(permanent.getId(), DoesntUntapEffect.class).stream()
                .anyMatch(floating -> floating.effect() instanceof DoesntUntapEffect lock
                        && (lock.condition() == UntapLockCondition.WHILE_SOURCE_ON_BATTLEFIELD
                        || lock.condition() == UntapLockCondition.WHILE_SOURCE_TAPPED));
    }

    private boolean hasActiveSelfDoesntUntap(GameData gameData, Permanent permanent, UUID controllerId,
                                              CardEffect effect) {
        if (effect instanceof DoesntUntapEffect doesNotUntap) {
            return doesNotUntap.subject() instanceof PermanentSubject.Source;
        }
        if (effect instanceof ConditionalEffect conditional) {
            return conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forStaticEffect(permanent, controllerId))
                    && hasActiveSelfDoesntUntap(gameData, permanent, controllerId, conditional.wrapped());
        }
        return false;
    }
}
