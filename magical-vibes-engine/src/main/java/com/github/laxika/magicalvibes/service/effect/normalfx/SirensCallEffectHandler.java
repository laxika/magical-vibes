package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DestroyNonAttackersAtEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SirensCallEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SirensCallEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID activePlayerId = gameData.activePlayerId;
        List<Permanent> battlefield = gameData.playerBattlefields.get(activePlayerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                // "if able" is enforced by combat: the must-attack requirement only applies to
                // creatures that can legally attack, so setting the flag on summoning-sick creatures
                // is harmless.
                if (gameQueryService.isCreature(gameData, permanent)) {
                    permanent.setMustAttackThisTurn(true);
                }
            }
        }

        gameData.queueDelayedAction(new DestroyNonAttackersAtEndStep(activePlayerId));

        String playerName = gameData.playerIdToName.get(activePlayerId);
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.text("Creatures " + playerName + " controls must attack this turn if able."));
        log.info("Game {} - Siren's Call forces {}'s creatures to attack this turn", gameData.id, playerName);
    }
}
