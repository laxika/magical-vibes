package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantAttackNextTurnCombatOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link CantAttackNextTurnCombatOpponentEffect}: flags the referenced combat opponent (the
 * creature Wall of Dust blocks, carried as the stack entry's target) so it can't attack during its
 * controller's next turn. The flag is promoted to an active restriction by the turn engine at the
 * start of that creature's controller's next turn.
 */
@Component
@RequiredArgsConstructor
public class CantAttackNextTurnCombatOpponentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantAttackNextTurnCombatOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }

        target.setCantAttackNextTurn(true);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " can't attack during its controller's next turn."));
    }
}
