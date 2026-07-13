package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect}: the target creature's
 * controller sacrifices it, then that same player creates one token per point of the sacrificed
 * creature's power (Mercy Killing).
 *
 * <p>The creature's effective power is captured before removal (static bonuses still apply), so the
 * token count reflects the creature as it existed on the battlefield.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeTargetCreatureThenCreateTokensEqualToPowerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificeTargetCreatureThenCreateTokensEqualToPowerEffect) effect;

        UUID targetId = entry.getTargetId();
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            log.info("Game {} - {} fizzles (no legal creature target)", gameData.id, entry.getCard().getName());
            return;
        }

        UUID controllerId = gameData.findControllerOf(target.getId());
        int power = gameQueryService.getEffectivePower(gameData, target);

        permanentRemovalService.removePermanentToGraveyard(gameData, target);

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " sacrifices " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} sacrifices {} for {}", gameData.id, playerName,
                target.getCard().getName(), entry.getCard().getName());

        if (power > 0) {
            permanentControlSupport.applyCreateToken(
                    gameData, controllerId, e.tokenTemplate(), power, entry.getCard().getSetCode());
        }
    }
}
