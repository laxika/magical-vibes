package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageAndDefendingPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link AssignNoCombatDamageAndDefendingPlayerDiscardsEffect} (Cloak of Confusion). The
 * enchanted attacker is the stack entry's {@code sourcePermanentId} and the defending player its
 * {@code targetId}: the attacker is added to {@code creaturesPreventedFromDealingCombatDamage} so it
 * deals no combat damage this turn (cleared at turn cleanup), and the defending player discards a
 * card at random.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignNoCombatDamageAndDefendingPlayerDiscardsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AssignNoCombatDamageAndDefendingPlayerDiscardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attackerId = entry.getSourcePermanentId();
        UUID defenderId = entry.getTargetId();
        if (attackerId == null || defenderId == null) {
            return;
        }

        gameData.creaturesPreventedFromDealingCombatDamage.add(attackerId);
        Permanent attacker = gameQueryService.findPermanentById(gameData, attackerId);
        String attackerName = attacker != null ? attacker.getCard().getName() : "the attacking creature";
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.text(attackerName + " assigns no combat damage this turn."));

        gameData.discardCausedByOpponent = true;
        playerInteractionSupport.resolveRandomDiscardCards(gameData, defenderId, entry.getCard().getName(), 1);
    }
}
