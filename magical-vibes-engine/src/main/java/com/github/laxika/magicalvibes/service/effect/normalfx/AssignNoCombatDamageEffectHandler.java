package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AssignNoCombatDamageEffect}: prevents the combat damage of the stack entry's {@code sourcePermanentId}
 * with a floating effect and makes it assign no combat damage with another (which unpreventable damage can't
 * override), until end of turn or end of combat.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignNoCombatDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AssignNoCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AssignNoCombatDamageEffect assignNone = (AssignNoCombatDamageEffect) effect;
        UUID attackerId = assignNone.useTriggeringPermanent() ? entry.getTriggeringPermanentId()
                : assignNone.useTargetId() ? entry.getTargetId() : entry.getSourcePermanentId();
        if (attackerId == null) {
            return;
        }

        boolean thisCombatOnly = ((AssignNoCombatDamageEffect) effect).thisCombatOnly();
        if (thisCombatOnly) {
            PreventionSupport.preventCombatDamageDealtBy(gameData, entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), attackerId, Duration.UNTIL_END_OF_COMBAT);
            PreventionSupport.assignNoCombatDamage(gameData, entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), attackerId, Duration.UNTIL_END_OF_COMBAT);
        } else {
            PreventionSupport.preventCombatDamageDealtBy(gameData, entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), attackerId, Duration.UNTIL_END_OF_TURN);
            PreventionSupport.assignNoCombatDamage(gameData, entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), attackerId, Duration.UNTIL_END_OF_TURN);
        }
        Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
        String attackerName = attacker != null ? attacker.getCard().getName() : "the attacking creature";
        String duration = thisCombatOnly ? "combat" : "turn";
        gameLogService.append(gameData,
                GameLog.text(attackerName + " assigns no combat damage this " + duration + "."));
        log.info("Game {} - {} assigns no combat damage this {}", gameData.id, attackerName, duration);
    }
}
