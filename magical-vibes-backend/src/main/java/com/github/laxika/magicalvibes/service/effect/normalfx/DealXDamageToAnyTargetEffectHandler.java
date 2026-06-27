package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealXDamageToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealXDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealXDamageToAnyTargetEffect) effect;

        UUID targetId = entry.getTargetId();
        if (targetId == null) return;

        // Mark the target creature for exile-instead-of-die before dealing damage,
        // so that if lethal damage destroys it immediately, the replacement applies.
        if (e.exileInsteadOfDie()) {
            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            if (!targetIsPlayer) {
                Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
                if (targetPermanent != null && gameQueryService.isCreature(gameData, targetPermanent)) {
                    targetPermanent.setExileInsteadOfDieThisTurn(true);
                }
            }
        }

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, entry.getXValue(), entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, false);
        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
