package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedRevealCreatureCardsToBattlefield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesYouControlAndRegisterDelayedRevealEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves the immediate exile and registers Synthetic Destiny's delayed replacement effect. */
@Component
@RequiredArgsConstructor
public class ExileAllCreaturesYouControlAndRegisterDelayedRevealEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileAllCreaturesYouControlAndRegisterDelayedRevealEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        List<Permanent> creatures = new ArrayList<>(
                gameData.playerBattlefields.get(controllerId).stream()
                        .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                        .toList());

        if (creatures.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    controllerName + " controls no creatures — no creatures are exiled."));
            return;
        }

        int exiledCount = 0;
        permanentRemovalService.beginPermanentLeaveBatch(gameData);
        try {
            for (Permanent creature : creatures) {
                if (permanentRemovalService.removePermanentToExile(gameData, creature)) {
                    exiledCount++;
                    gameLogService.append(gameData, GameLog.cardThen(
                            creature.getCard(), " is exiled."));
                }
            }
        } finally {
            permanentRemovalService.endPermanentLeaveBatch(gameData);
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (exiledCount == 0) {
            return;
        }
        gameData.queueDelayedAction(new DelayedRevealCreatureCardsToBattlefield(
                controllerId, entry.getCard(), exiledCount));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " registers a delayed ability to reveal creature cards at the beginning of the next end step."));
    }
}
