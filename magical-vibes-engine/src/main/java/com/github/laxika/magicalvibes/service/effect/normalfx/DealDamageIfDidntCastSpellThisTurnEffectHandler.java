package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageIfDidntCastSpellThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageIfDidntCastSpellThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageIfDidntCastSpellThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageIfDidntCastSpellThisTurnEffect) effect;

        UUID targetId = entry.getTargetId();
        String cardName = entry.getCard().getName();

        if (!gameData.playerIds.contains(targetId)) return;

        // Intervening-if: re-check condition at resolution time. A countered spell still counts.
        if (gameData.getSpellsCastThisTurnCount(targetId) > 0) {
            String playerName = gameData.playerIdToName.get(targetId);
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + "'s ability does nothing — " + playerName + " cast a spell this turn.");
            return;
        }

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
