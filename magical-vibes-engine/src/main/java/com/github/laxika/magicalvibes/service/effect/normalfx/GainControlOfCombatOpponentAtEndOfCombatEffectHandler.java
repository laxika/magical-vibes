package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.GainControlOfPermanentAtEndOfCombat;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link GainControlOfCombatOpponentAtEndOfCombatEffect}: if the referenced combat opponent
 * (carried as the stack entry's target) is a creature, schedule its controller to change to the
 * source's controller at end of combat via {@link GainControlOfPermanentAtEndOfCombat}. The
 * Wretched-style "At end of combat, gain control of all creatures blocking this creature for as long
 * as you control this creature."
 */
@Component
@RequiredArgsConstructor
public class GainControlOfCombatOpponentAtEndOfCombatEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

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

        gameData.queueDelayedAction(new GainControlOfPermanentAtEndOfCombat(
                targetId, entry.getControllerId(), sourceId, entry.getCard().getName()));
        String logEntry = entry.getCard().getName() + " will gain control of "
                + target.getCard().getName() + " at end of combat.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(entry.getCard(), " will gain control of ", target.getCard(), " at end of combat."));
    }
}
