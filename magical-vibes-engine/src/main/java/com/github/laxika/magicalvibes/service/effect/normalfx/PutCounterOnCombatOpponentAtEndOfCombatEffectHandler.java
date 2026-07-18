package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PutCounterOnPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCounterOnCombatOpponentAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCounterOnCombatOpponentAtEndOfCombatEffect counterEffect =
                (PutCounterOnCombatOpponentAtEndOfCombatEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null || counterEffect.amount() <= 0) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        gameData.queueDelayedAction(new PutCounterOnPermanentAtEndOfCombat(
                targetId, counterEffect.counterType(), counterEffect.amount()));
        String logEntry = target.getCard().getName() + " will get " + counterEffect.amount()
                + " counter(s) at end of combat.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(target.getCard()).text(" will get " + counterEffect.amount() + " counter(s) at end of combat.").build());
    }
}
