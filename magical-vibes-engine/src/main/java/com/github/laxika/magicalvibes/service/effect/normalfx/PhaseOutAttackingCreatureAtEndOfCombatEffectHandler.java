package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PhaseOutAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutAttackingCreatureAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PhaseOutAttackingCreatureAtEndOfCombatEffect}: schedule the triggering attacker
 * (the stack entry's non-targeting {@code targetId}) to phase out at end of combat via
 * {@link PhaseOutAtEndOfCombat}. Teferi's Veil.
 */
@Component
@RequiredArgsConstructor
public class PhaseOutAttackingCreatureAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutAttackingCreatureAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent attacker = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (attacker == null) {
            return;
        }
        gameData.queueDelayedAction(new PhaseOutAtEndOfCombat(attacker.getId()));
        gameLogService.append(gameData, GameLog.cardThen(attacker.getCard(), " will phase out at end of combat."));
    }
}
