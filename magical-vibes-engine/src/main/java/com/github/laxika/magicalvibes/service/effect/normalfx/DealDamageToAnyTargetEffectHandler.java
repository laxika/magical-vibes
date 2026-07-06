package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToAnyTargetEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAnyTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToAnyTargetEffect) effect;

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

        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (e.g. sacrificed as an activation cost).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));

        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, e.cantRegenerate());
        gameOutcomeService.checkWinCondition(gameData);

    }
}
