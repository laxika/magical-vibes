package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.MustAttackIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DealDamageToPermanentsTargetControlsEffect}: each permanent of its subject, found once through
 * {@link SubjectResolutionService}, is dealt the damage, evaluated once; nothing happens when a {@code controlledBy}
 * subject's players name nobody. Protection and every prevention are options of each permanent's damage pool, ordered
 * by its controller (CR 616.1).
 */
@Component
@RequiredArgsConstructor
public class DealDamageToPermanentsTargetControlsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToPermanentsTargetControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToPermanentsTargetControlsEffect) effect;
        entry.recordStep(StepKind.DEAL_DAMAGE, StepOutcome.NONE);

        if (e.subject() instanceof PermanentSubject.ControlledBy damaged
                && subjectResolutionService.players(gameData, entry, e, damaged.players()).isEmpty()) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int rawDamage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));

        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, e, e.subject())) {
            // Protection and every prevention are options of the permanent's damage pool (CR 616.1, 702.16e).
            int damageDealt = damageSupport.dealCreatureDamage(gameData, entry, permanent, rawDamage);
            // "Each creature dealt damage this way …" (Aggravate): a creature whose damage was
            // fully prevented or redirected away never took damage, so it isn't affected.
            if (e.damagedCreaturesMustAttackThisTurn() && damageDealt > 0) {
                gameData.addEffectOnPermanent(entry, new MustAttackIfAbleEffect(), permanent.getId(), Duration.UNTIL_END_OF_TURN);
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
