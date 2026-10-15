package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndOfCombatTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link GainControlOfCombatOpponentAtEndOfCombatEffect}: if the referenced combat opponent
 * (carried as the stack entry's target) is a creature, creates a delayed "at end of combat" trigger
 * that gains control of it for the effect's duration (Tolarian Entrancer).
 */
@Component
@RequiredArgsConstructor
public class GainControlOfCombatOpponentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfCombatOpponentAtEndOfCombatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        UUID sourceId = entry.getSourcePermanentId();
        if (targetId == null || sourceId == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        GainControlOfCombatOpponentAtEndOfCombatEffect controlEffect =
                (GainControlOfCombatOpponentAtEndOfCombatEffect) effect;
        gameData.queueDelayedAction(new DelayedEndOfCombatTrigger(entry.getControllerId(), entry.getCard(),
                sourceId, targetId, new GainControlOfTargetEffect(controlEffect.duration())));
        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " will gain control of ", target.getCard(), " at end of combat."));
    }
}
