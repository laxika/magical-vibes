package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link SacrificeCombatOpponentAtEndOfCombatEffect}: schedules the referenced combat
 * opponent (carried as the stack entry's non-targeting target) for sacrifice at end of combat,
 * carrying the effect's optional token rider so the sacrificing player gets it only if the
 * sacrifice actually happens. See Basalt Golem.
 */
@Component
@RequiredArgsConstructor
public class SacrificeCombatOpponentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeCombatOpponentAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SacrificeCombatOpponentAtEndOfCombatEffect sacrificeEffect =
                (SacrificeCombatOpponentAtEndOfCombatEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        gameData.queueDelayedAction(new SacrificeAtEndOfCombat(targetId, null, entry.getCard(), 0,
                sacrificeEffect.tokenForSacrificingPlayer()));
        gameLogService.append(gameData,
                GameLog.cardThen(target.getCard(), " will be sacrificed at end of combat."));
    }
}
