package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.MustAttackIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPermanentsTargetControlsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DealDamageToPermanentsTargetControlsEffect}: each permanent of its {@code controlledBy} subject, found
 * once through {@link SubjectResolutionService}, is dealt the damage; nothing happens when the subject's players name
 * nobody.
 */
@Component
@RequiredArgsConstructor
public class DealDamageToPermanentsTargetControlsEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToPermanentsTargetControlsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToPermanentsTargetControlsEffect) effect;

        var damaged = (PermanentSubject.ControlledBy) e.subject();
        if (subjectResolutionService.players(gameData, entry, e, damaged.players()).isEmpty()) return;

        int rawDamage = e.damage();
        String cardName = entry.getCard().getName();

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, e, damaged)) {
            if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, permanent, entry.getCard(), entry.getControllerId())) {
                gameLogService.append(gameData, GameLog.textCardText(cardName + "'s damage to ", permanent.getCard(), " is prevented."));
                continue;
            }
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
