package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AssignNoCombatDamageEffect}: adds the stack entry's {@code sourcePermanentId} to
 * {@code creaturesPreventedFromDealingCombatDamage} so that creature deals no combat damage this
 * turn (cleared at turn cleanup).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignNoCombatDamageEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AssignNoCombatDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attackerId = entry.getSourcePermanentId();
        if (attackerId == null) {
            return;
        }

        gameData.creaturesPreventedFromDealingCombatDamage.add(attackerId);
        Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
        String attackerName = attacker != null ? attacker.getCard().getName() : "the attacking creature";
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.text(attackerName + " assigns no combat damage this turn."));
        log.info("Game {} - {} assigns no combat damage this turn", gameData.id, attackerName);
    }
}
