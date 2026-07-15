package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaClashEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ManaClashEffect}: the controller and the targeted opponent repeatedly flip coins,
 * each player whose coin is tails takes 1 damage, and the loop stops once both coins come up heads
 * on the same flip. Damage runs through {@link DamageSupport#dealDamageToPlayer} so prevention and
 * damage-multiplier effects (e.g. Furnace of Rath) still apply.
 */
@Component
@RequiredArgsConstructor
public class ManaClashEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManaClashEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        if (!gameData.playerIds.contains(opponentId)) {
            return;
        }

        String sourceName = entry.getCard().getName();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String opponentName = gameData.playerIdToName.get(opponentId);

        while (true) {
            boolean controllerHeads = ThreadLocalRandom.current().nextBoolean();
            boolean opponentHeads = ThreadLocalRandom.current().nextBoolean();

            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(sourceName + ": "
                    + controllerName + " flips " + (controllerHeads ? "heads" : "tails") + ", "
                    + opponentName + " flips " + (opponentHeads ? "heads" : "tails") + "."));

            if (!controllerHeads) {
                dealOneDamage(gameData, entry, controllerId);
            }
            if (!opponentHeads) {
                dealOneDamage(gameData, entry, opponentId);
            }

            gameOutcomeService.checkWinCondition(gameData);

            if (controllerHeads && opponentHeads) {
                break;
            }
        }
    }

    private void dealOneDamage(GameData gameData, StackEntry entry, UUID playerId) {
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, 1, entry);
        damageSupport.dealDamageToPlayer(gameData, entry, playerId, rawDamage);
    }
}
