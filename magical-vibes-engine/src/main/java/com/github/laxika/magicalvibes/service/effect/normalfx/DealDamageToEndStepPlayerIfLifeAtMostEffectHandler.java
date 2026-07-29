package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerIfLifeAtMostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToEndStepPlayerIfLifeAtMostEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEndStepPlayerIfLifeAtMostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToEndStepPlayerIfLifeAtMostEffect) effect;

        UUID targetId = entry.getTargetId();
        if (!gameData.playerIds.contains(targetId)) return;

        // Intervening-if: re-check the life threshold at resolution time (CR 603.4).
        if (gameData.playerLifeTotals.getOrDefault(targetId, 20) > e.lifeThreshold()) {
            String playerName = gameData.playerIdToName.get(targetId);
            gameLogService.append(gameData, GameLog.text(entry.getCard().getName()
                    + "'s ability does nothing — " + playerName + " has more than " + e.lifeThreshold() + " life."));
            return;
        }

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
