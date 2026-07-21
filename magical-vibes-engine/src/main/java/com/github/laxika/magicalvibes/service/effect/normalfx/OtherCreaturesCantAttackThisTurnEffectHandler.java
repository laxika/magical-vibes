package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OtherCreaturesCantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link OtherCreaturesCantAttackThisTurnEffect} (Intimidation Bolt's rider) by appending the
 * targeted creature's permanent ID to {@link GameData#otherCreaturesCantAttackExemptCreatureIds}. That
 * list is read by {@code CombatAttackService.canCreatureAttack}: a creature may attack only if its ID
 * equals every exemption, so only the creature Intimidation Bolt targeted (if it survived the
 * accompanying damage) may attack this turn. If the target died to the damage, its ID matches no living
 * creature and nothing may attack (CR-accurate). Cleared at the next turn transition.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtherCreaturesCantAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OtherCreaturesCantAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID exemptCreatureId = entry.getTargetId();
        gameData.otherCreaturesCantAttackExemptCreatureIds.add(exemptCreatureId);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text("Other creatures can't attack this turn."));
        log.info("Game {} - other creatures (except {}) can't attack this turn", gameData.id, exemptCreatureId);
    }
}
