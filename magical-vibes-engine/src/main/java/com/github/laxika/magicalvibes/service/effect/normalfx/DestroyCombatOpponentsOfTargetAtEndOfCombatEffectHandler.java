package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DestroyCombatOpponentsAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentsOfTargetAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DestroyCombatOpponentsOfTargetAtEndOfCombatEffect} by queueing a
 * {@link DestroyCombatOpponentsAtEndOfCombat} delayed action for the target. The creatures to
 * destroy are only determined when that action is drained at end of combat, so blocks declared
 * after this resolution still count (Venomous Breath and Glyph of Doom).
 */
@Component
@RequiredArgsConstructor
public class DestroyCombatOpponentsOfTargetAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyCombatOpponentsOfTargetAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DestroyCombatOpponentsOfTargetAtEndOfCombatEffect destroyEffect =
                (DestroyCombatOpponentsOfTargetAtEndOfCombatEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }
        gameData.queueDelayedAction(new DestroyCombatOpponentsAtEndOfCombat(target.getId(),
                destroyEffect.onlyCreaturesBlockedByTarget()));
        String affectedCreatures = destroyEffect.onlyCreaturesBlockedByTarget()
                ? "'s blocked creatures will be destroyed at end of combat."
                : "'s combat opponents will be destroyed at end of combat.";
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), affectedCreatures));
    }
}
