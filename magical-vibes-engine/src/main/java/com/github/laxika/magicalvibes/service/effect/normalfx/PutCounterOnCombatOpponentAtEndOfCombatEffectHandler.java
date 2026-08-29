package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.Set;

/**
 * Resolves {@link PutCounterOnCombatOpponentAtEndOfCombatEffect}: if the referenced combat opponent
 * (carried as the stack entry's target) is a creature, schedule it to receive the configured
 * counters at end of combat via {@link PutCounterOnPermanentAtEndOfCombat}. Greater Werewolf-style
 * "put a -0/-2 counter on each creature blocking or blocked by this creature."
 */
@Component
@RequiredArgsConstructor
public class PutCounterOnCombatOpponentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnCombatOpponentAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterOnCombatOpponentAtEndOfCombatEffect counterEffect =
                (PutCounterOnCombatOpponentAtEndOfCombatEffect) effect;

        UUID targetId = entry.getTargetId();
        if (counterEffect.amount() <= 0) {
            return;
        }
        if (targetId == null && gameData.currentStep == TurnStep.END_OF_COMBAT) {
            Set<UUID> combatOpponentIds = gameData.combatBlockOpponentIdsThisCombat
                    .getOrDefault(entry.getSourcePermanentId(), Set.of());
            for (UUID combatOpponentId : combatOpponentIds) {
                Permanent combatOpponent = gameQueryService.findPermanentById(gameData, combatOpponentId);
                if (combatOpponent != null && gameQueryService.isCreature(gameData, combatOpponent)) {
                    permanentCounterSupport.placeCounterOnPermanent(gameData, entry, combatOpponent,
                            counterEffect.counterType(), counterEffect.amount());
                }
            }
            return;
        }
        if (targetId == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        gameData.queueDelayedAction(new PutCounterOnPermanentAtEndOfCombat(
                targetId, counterEffect.counterType(), counterEffect.amount(), counterEffect.alsoTap()));
        String tapSuffix = counterEffect.alsoTap() ? " and become tapped" : "";
        gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                .text(" will get " + counterEffect.amount() + " counter(s)" + tapSuffix + " at end of combat.")
                .build());
    }
}
