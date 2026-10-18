package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DestroyNonAttackersAtEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SirensCallEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Siren's Call: every creature the active player controls must attack this turn if able, and the
 * non-attackers are scheduled for destruction at the beginning of the next end step (via a
 * {@link DestroyNonAttackersAtEndStep} delayed action drained in {@code StepTriggerService}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SirensCallEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SirensCallEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var call = (SirensCallEffect) effect;
        UUID activePlayerId = gameData.activePlayerId;
        // The must-attack requirement is read from the delayed destruction action for every creature the
        // player controls (CR 611.2c: a rules-modifying effect also covers creatures that arrive later).
        gameData.queueDelayedAction(new DestroyNonAttackersAtEndStep(activePlayerId, entry.getCard(),
                call.excludeWallsFromAttack(), call.excludeSummoningSickFromDestruction(),
                entry.getControllerId()));

        String playerName = gameData.playerIdToName.get(activePlayerId);
        gameLogService.append(gameData,
                GameLog.text("Creatures " + playerName + " controls must attack this turn if able."));
        log.info("Game {} - Siren's Call forces {}'s creatures to attack this turn", gameData.id, playerName);
    }
}
