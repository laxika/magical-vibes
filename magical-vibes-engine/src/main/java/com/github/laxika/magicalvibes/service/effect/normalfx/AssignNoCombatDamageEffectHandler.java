package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AssignNoCombatDamageEffect}: prevents the combat damage of each creature its subject names (one that
 * left included) with a floating effect and makes it assign no combat damage with another (which unpreventable damage can't
 * override), until end of turn or end of combat.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignNoCombatDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AssignNoCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AssignNoCombatDamageEffect assignNone = (AssignNoCombatDamageEffect) effect;
        boolean thisCombatOnly = assignNone.thisCombatOnly();
        Duration duration = thisCombatOnly ? Duration.UNTIL_END_OF_COMBAT : Duration.UNTIL_END_OF_TURN;
        for (UUID attackerId : subjectResolutionService.permanentIds(gameData, entry, effect, assignNone.subject())) {
            PreventionSupport.preventCombatDamageDealtBy(gameData, entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), attackerId, duration);
            PreventionSupport.assignNoCombatDamage(gameData, entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(), attackerId, duration);
            Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
            String attackerName = attacker != null
                    ? FaceDownVisibility.publicName(gameData, attacker) : "the attacking creature";
            String span = thisCombatOnly ? "combat" : "turn";
            gameLogService.append(gameData,
                    GameLog.text(attackerName + " assigns no combat damage this " + span + "."));
            log.info("Game {} - {} assigns no combat damage this {}", gameData.id, attackerName, span);
        }
    }
}
