package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OtherCreaturesCantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link OtherCreaturesCantAttackThisTurnEffect} (Intimidation Bolt's rider) by appending the
 * targeted creature's permanent ID as the exemption of a floating effect that lasts until end of turn. The
 * effects are read by {@code AttackLegalityService.canAttack}: a creature may attack only if its ID
 * equals every exemption, so only the creature Intimidation Bolt targeted (if it survived the
 * accompanying damage) may attack this turn. If the target died to the damage, its ID matches no living
 * creature and nothing may attack (CR-accurate).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtherCreaturesCantAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OtherCreaturesCantAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID exemptCreatureId = entry.getTargetId();
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                entry.getSourcePermanentId(), entry.getControllerId(),
                new OtherCreaturesCantAttackThisTurnEffect(exemptCreatureId), null, Duration.UNTIL_END_OF_TURN));

        gameLogService.append(gameData, GameLog.text("Other creatures can't attack this turn."));
        log.info("Game {} - other creatures (except {}) can't attack this turn", gameData.id, exemptCreatureId);
    }
}
